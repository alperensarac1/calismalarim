package com.example.dosyapaylasimjava;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dosyapaylasimjava.model.LinkResponse;
import com.example.dosyapaylasimjava.model.UploadResponse;
import com.example.dosyapaylasimjava.service.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvSelectedFile;
    private Button btnPickFile;
    private Button btnUpload;
    private ProgressBar progressUpload;
    private TextView tvUploadResult;
    private Button btnCopyLink;
    private String lastDownloadUrl = null;

    private EditText etCode;
    private Button btnCheck;
    private Button btnDownload;
    private TextView tvCodeResult;

    private Uri pickedUri = null;
    private String pickedDisplayName = null;
    private long pickedSize = -1L;

    private final ActivityResultLauncher<String[]> pickFileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    pickedUri = uri;
                    queryMeta(uri);
                    tvSelectedFile.setText("Seçili dosya: " +
                            (pickedDisplayName != null ? pickedDisplayName : uri.getLastPathSegment()));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML'ini kendi dosya adınla eşleştir

        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        btnPickFile = findViewById(R.id.btnPickFile);
        btnUpload = findViewById(R.id.btnUpload);
        progressUpload = findViewById(R.id.progressUpload);
        tvUploadResult = findViewById(R.id.tvUploadResult);
        btnCopyLink = findViewById(R.id.btnCopyLink);

        etCode = findViewById(R.id.etCode);
        btnCheck = findViewById(R.id.btnCheck);
        btnDownload = findViewById(R.id.btnDownload);
        tvCodeResult = findViewById(R.id.tvCodeResult);

        btnPickFile.setOnClickListener(v -> openPicker());
        btnUpload.setOnClickListener(v -> uploadPicked());
        btnCheck.setOnClickListener(v -> checkCode());
        btnDownload.setOnClickListener(v -> downloadByCode());

        btnCopyLink.setOnClickListener(v -> {
            String url = lastDownloadUrl;
            if (!TextUtils.isEmpty(url)) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    ClipData clip = ClipData.newPlainText("download link", url);
                    cm.setPrimaryClip(clip);
                    Toast.makeText(this, "Link panoya kopyalandı", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Henüz bir link yok", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPicker() {
        // Storage Access Framework
        pickFileLauncher.launch(new String[]{"*/*"});
    }

    private void queryMeta(Uri uri) {
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(uri, null, null, null, null);
        if (c != null) {
            try {
                int nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIdx = c.getColumnIndex(OpenableColumns.SIZE);
                if (c.moveToFirst()) {
                    pickedDisplayName = nameIdx >= 0 ? c.getString(nameIdx) : null;
                    pickedSize = sizeIdx >= 0 ? c.getLong(sizeIdx) : -1L;
                }
            } finally {
                c.close();
            }
        }
    }

    private void uploadPicked() {
        if (pickedUri == null) {
            Toast.makeText(this, "Önce dosya seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressUpload.setVisibility(View.VISIBLE);
        tvUploadResult.setText("");

        byte[] bytes;
        try {
            bytes = readAllBytes(getContentResolver().openInputStream(pickedUri));
        } catch (IOException e) {
            progressUpload.setVisibility(View.GONE);
            Toast.makeText(this, "Dosya açılamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        String guessType = getContentResolver().getType(pickedUri);
        if (guessType == null) guessType = "application/octet-stream";

        RequestBody reqBody = RequestBody.create(MediaType.parse(guessType), bytes);
        String fileName = pickedDisplayName != null ? pickedDisplayName : "file";
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, reqBody);

        RetrofitClient.getApi().uploadFile(part).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                progressUpload.setVisibility(View.GONE);
                UploadResponse body = response.body();

                if (response.isSuccessful() && body != null) {
                    if (Boolean.TRUE.equals(body.getOk())) {
                        lastDownloadUrl = body.getDownloadUrl();
                        btnCopyLink.setVisibility(View.VISIBLE);

                        tvUploadResult.setTextColor(0xFF2E7D32); // yeşil
                        tvUploadResult.setText(
                                "Yüklendi! Kod: " + body.getCode() +
                                        "\nİndirme: " + body.getDownloadUrl() +
                                        "\nBilgi: " + body.getInfoUrl() +
                                        "\nGeçerlilik: " + body.getExpiresAt()
                        );
                        if (body.getCode() != null) {
                            etCode.setText(body.getCode());
                        }
                    } else {
                        tvUploadResult.setTextColor(0xFFB00020); // kırmızı
                        tvUploadResult.setText("Hata: " + (body.getError() != null ? body.getError() : "Bilinmeyen"));
                        btnCopyLink.setVisibility(View.GONE);
                        lastDownloadUrl = null;
                    }
                } else {
                    tvUploadResult.setTextColor(0xFFB00020);
                    tvUploadResult.setText("Sunucu hatası: " + response.code());
                    btnCopyLink.setVisibility(View.GONE);
                    lastDownloadUrl = null;
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                progressUpload.setVisibility(View.GONE);
                tvUploadResult.setTextColor(0xFFB00020);
                tvUploadResult.setText("İstek hatası: " + t.getMessage());
            }
        });
    }

    private void checkCode() {
        String code = etCode.getText().toString().trim().toUpperCase(Locale.ROOT);
        if (!Pattern.matches("^[A-Z0-9]{6}$", code)) {
            Toast.makeText(this, "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show();
            return;
        }
        tvCodeResult.setText("Sorgulanıyor…");

        RetrofitClient.getApi().getLink(code).enqueue(new Callback<LinkResponse>() {
            @Override
            public void onResponse(Call<LinkResponse> call, Response<LinkResponse> response) {
                LinkResponse body = response.body();
                if (response.isSuccessful() && body != null && Boolean.TRUE.equals(body.getOk())) {
                    if (Boolean.TRUE.equals(body.getExpired())) {
                        tvCodeResult.setTextColor(0xFFB06D00); // turuncu
                        tvCodeResult.setText("Kod: " + body.getCode() + " — Süresi dolmuş veya pasif.");
                    } else {
                        tvCodeResult.setTextColor(0xFF0D47A1); // mavi
                        tvCodeResult.setText(
                                "Kod: " + body.getCode() +
                                        "\nDosya: " + body.getOriginalName() +
                                        "\nBoyut: " + body.getSizeBytes() +
                                        "\nSon Kullanım: " + body.getExpiresAt() +
                                        "\nLink: " + body.getDownloadUrl()
                        );
                    }
                } else {
                    tvCodeResult.setTextColor(0xFFB00020);
                    String err = (body != null && body.getError() != null)
                            ? body.getError()
                            : ("Sunucu hatası " + response.code());
                    tvCodeResult.setText("Hata: " + err);
                }
            }

            @Override
            public void onFailure(Call<LinkResponse> call, Throwable t) {
                tvCodeResult.setTextColor(0xFFB00020);
                tvCodeResult.setText("İstek hatası: " + t.getMessage());
            }
        });
    }

    private void downloadByCode() {
        String code = etCode.getText().toString().trim().toUpperCase(Locale.ROOT);
        if (!Pattern.matches("^[A-Z0-9]{6}$", code)) {
            Toast.makeText(this, "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "https://alperensaracdeneme.com/api/download.php?code=" + code;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "İndirme bağlantısı açılamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        if (is == null) return new byte[0];
        try (InputStream in = is; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        }
    }
}
