package com.example.pdfconverterjava;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.pdfconverterjava.data.model.CreateJobResponse;
import com.example.pdfconverterjava.data.model.JobStatusResponse;
import com.example.pdfconverterjava.data.repository.PdfRepository;
import com.example.pdfconverterjava.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private ProgressBar progressBar;
    private Button btnOpenResult;

    private PdfRepository repository;

    private String pendingJobType = null;
    private String latestResultUrl = null;

    private final int userId = 1;

    private ActivityResultLauncher<String[]> singleFilePicker;
    private ActivityResultLauncher<String[]> multiFilePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new PdfRepository();

        initViews();
        initPickers();
        initClickListeners();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);
        btnOpenResult = findViewById(R.id.btnOpenResult);
    }

    private void initPickers() {
        singleFilePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;

                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    File file = FileUtils.copyUriToFile(this, uri);

                    if (file == null || pendingJobType == null || pendingJobType.trim().isEmpty()) {
                        tvStatus.setText("Dosya hazırlanamadı.");
                        return;
                    }

                    createSingleFileJob(userId, pendingJobType, file);
                }
        );

        multiFilePicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris == null || uris.isEmpty()) return;

                    List<Uri> uriList = new ArrayList<>(uris);
                    List<File> files = FileUtils.copyUrisToFiles(this, uriList);

                    if (files.isEmpty()) {
                        tvStatus.setText("Dosyalar hazırlanamadı.");
                        return;
                    }

                    createMultiFileJob(userId, "pdf_merge", files);
                }
        );
    }

    private void initClickListeners() {
        findViewById(R.id.btnSelectJpgToPdf).setOnClickListener(v -> {
            pendingJobType = "jpg_to_pdf";
            singleFilePicker.launch(new String[]{"image/*"});
        });

        findViewById(R.id.btnSelectPdfToWord).setOnClickListener(v -> {
            pendingJobType = "pdf_to_word";
            singleFilePicker.launch(new String[]{"application/pdf"});
        });

        findViewById(R.id.btnSelectWordToPdf).setOnClickListener(v -> {
            pendingJobType = "word_to_pdf";
            singleFilePicker.launch(new String[]{
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword",
                    "*/*"
            });
        });

        findViewById(R.id.btnSelectMergePdf).setOnClickListener(v -> {
            multiFilePicker.launch(new String[]{"application/pdf"});
        });

        findViewById(R.id.btnGoToHistory).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, JobsActivity.class);
            startActivity(intent);
        });

        btnOpenResult.setOnClickListener(v -> {
            if (latestResultUrl != null && !latestResultUrl.trim().isEmpty()) {
                openUrl(latestResultUrl);
            }
        });
    }

    private void createSingleFileJob(int userId, String jobType, File file) {
        showLoading("Dosya yükleniyor ve job oluşturuluyor...");

        repository.createSingleFileJob(userId, jobType, file).enqueue(new Callback<CreateJobResponse>() {
            @Override
            public void onResponse(Call<CreateJobResponse> call, Response<CreateJobResponse> response) {
                hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    CreateJobResponse body = response.body();

                    if (body.isSuccess() && body.getJob_id() != null) {
                        tvStatus.setText(body.getMessage() != null ? body.getMessage() : "Job oluşturuldu");
                        startPolling(body.getJob_id());
                    } else {
                        tvStatus.setText(body.getMessage() != null ? body.getMessage() : "Job oluşturulamadı");
                    }
                } else {
                    tvStatus.setText("Sunucu hatası: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CreateJobResponse> call, Throwable t) {
                hideLoading();
                tvStatus.setText("Hata: " + t.getMessage());
            }
        });
    }

    private void createMultiFileJob(int userId, String jobType, List<File> files) {
        showLoading("Dosyalar yükleniyor ve merge job oluşturuluyor...");

        repository.createMultiFileJob(userId, jobType, files).enqueue(new Callback<CreateJobResponse>() {
            @Override
            public void onResponse(Call<CreateJobResponse> call, Response<CreateJobResponse> response) {
                hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    CreateJobResponse body = response.body();

                    if (body.isSuccess() && body.getJob_id() != null) {
                        tvStatus.setText(body.getMessage() != null ? body.getMessage() : "Merge job oluşturuldu");
                        startPolling(body.getJob_id());
                    } else {
                        tvStatus.setText(body.getMessage() != null ? body.getMessage() : "Job oluşturulamadı");
                    }
                } else {
                    tvStatus.setText("Sunucu hatası: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CreateJobResponse> call, Throwable t) {
                hideLoading();
                tvStatus.setText("Hata: " + t.getMessage());
            }
        });
    }

    private android.os.Handler pollingHandler = new android.os.Handler(getMainLooper());
    private Runnable pollingRunnable;

    private void startPolling(int jobId) {
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Job durumu kontrol ediliyor...");

        if (pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                repository.getJobStatus(jobId).enqueue(new Callback<JobStatusResponse>() {
                    @Override
                    public void onResponse(Call<JobStatusResponse> call, Response<JobStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JobStatusResponse body = response.body();
                            String status = body.getStatus();

                            if ("waiting".equals(status)) {
                                tvStatus.setText("İş sıraya alındı, worker bekleniyor...");
                                progressBar.setVisibility(View.VISIBLE);
                                pollingHandler.postDelayed(pollingRunnable, 3000);

                            } else if ("processing".equals(status)) {
                                tvStatus.setText("Dönüştürme işlemi devam ediyor...");
                                progressBar.setVisibility(View.VISIBLE);
                                pollingHandler.postDelayed(pollingRunnable, 3000);

                            } else if ("done".equals(status)) {
                                progressBar.setVisibility(View.GONE);
                                latestResultUrl = body.getResult_file_url();
                                tvStatus.setText("İşlem tamamlandı. Sonuç hazır.");

                                if (latestResultUrl != null && URLUtil.isValidUrl(latestResultUrl)) {
                                    btnOpenResult.setVisibility(View.VISIBLE);
                                }

                            } else if ("failed".equals(status)) {
                                progressBar.setVisibility(View.GONE);
                                tvStatus.setText("Hata: " + (body.getError_message() != null
                                        ? body.getError_message()
                                        : "İşlem başarısız"));

                            } else {
                                progressBar.setVisibility(View.GONE);
                                tvStatus.setText("Bilinmeyen durum: " + status);
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            tvStatus.setText("Durum sorgulama hatası: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<JobStatusResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Durum sorgulama hatası: " + t.getMessage());
                    }
                });
            }
        };

        pollingHandler.post(pollingRunnable);
    }
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void showLoading(String text) {
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText(text);
        btnOpenResult.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }
}