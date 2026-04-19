package com.example.resimarkaplankaldrmajava;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwnerKt;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Job;

public class MainActivity extends AppCompatActivity {

    private ZoomableImageView imageView;
    private Button btnSelectImage;
    private Button btnUndo;
    private Button btnReset;
    private Button btnSave;
    private TextView tvInfo;
    private TextView tvTolerance;
    private SeekBar seekTolerance;

    // İlk yüklenen görsel
    private Bitmap originalBitmap;

    // Kullanıcıya gösterilen aktif bitmap
    private Bitmap workingBitmap;

    // UI state
    private int tolerance = 60;

    // İşlem durumu
    private boolean isProcessing = false;

    // Aktif canlı preview state
    private boolean hasActivePreview = false;
    private Bitmap previewBaseBitmap;
    private int lastTappedX = -1;
    private int lastTappedY = -1;

    // Undo geçmişi
    private final ArrayDeque<Bitmap> undoStack = new ArrayDeque<>();
    private final int maxUndoCount = 10;

    // Arka plan işlemleri için
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Canlı önizlemede eski işi yok saymak için sürüm numarası
    private int previewGeneration = 0;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    loadSelectedImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUndo = findViewById(R.id.btnUndo);
        btnReset = findViewById(R.id.btnReset);
        btnSave = findViewById(R.id.btnSave);
        tvInfo = findViewById(R.id.tvInfo);
        tvTolerance = findViewById(R.id.tvTolerance);
        seekTolerance = findViewById(R.id.seekTolerance);

        setupUI();
        updateUndoButtonState();
    }

    private void setupUI() {
        btnSelectImage.setOnClickListener(v -> {
            if (isProcessing) return;
            pickImageLauncher.launch("image/*");
        });

        btnUndo.setOnClickListener(v -> {
            if (isProcessing) return;
            undoLastAction();
        });

        btnReset.setOnClickListener(v -> {
            if (isProcessing) return;
            resetImage();
        });

        btnSave.setOnClickListener(v -> {
            if (isProcessing) return;
            commitActivePreviewIfNeeded();
            saveTransparentImage();
        });

        seekTolerance.setProgress(tolerance);
        tvTolerance.setText("Tolerans: " + tolerance);

        seekTolerance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tolerance = progress;
                tvTolerance.setText("Tolerans: " + tolerance);

                if (fromUser) {
                    updateLivePreviewForCurrentSelection();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        imageView.setOnImageTapListener((bitmapX, bitmapY) -> {
            if (!isProcessing) {
                onBitmapTapped(bitmapX, bitmapY);
            }
        });
    }

    private void loadSelectedImage(Uri uri) {
        isProcessing = true;

        executor.execute(() -> {
            Bitmap bitmap = null;

            try {
                InputStream input = getContentResolver().openInputStream(uri);
                if (input != null) {
                    Bitmap decoded = BitmapFactory.decodeStream(input);
                    input.close();

                    if (decoded != null) {
                        bitmap = decoded.copy(Bitmap.Config.ARGB_8888, true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap finalBitmap = bitmap;
            runOnUiThread(() -> {
                isProcessing = false;

                if (finalBitmap == null) {
                    Toast.makeText(MainActivity.this, "Resim yüklenemedi", Toast.LENGTH_SHORT).show();
                    return;
                }

                originalBitmap = finalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                workingBitmap = finalBitmap.copy(Bitmap.Config.ARGB_8888, true);

                clearUndoHistory();
                clearActivePreview();

                imageView.setBitmap(workingBitmap);
                imageView.forceResetZoom();

                tvInfo.setText("Fotoğraf yüklendi. Silmek istediğin bölgeye dokun. Tolerans değişince sonuç canlı güncellenecek.");
                updateUndoButtonState();
            });
        });
    }

    private void onBitmapTapped(int bitmapX, int bitmapY) {
        Bitmap currentBitmap = workingBitmap;
        if (currentBitmap == null) return;

        if (bitmapX < 0 || bitmapX >= currentBitmap.getWidth()
                || bitmapY < 0 || bitmapY >= currentBitmap.getHeight()) {
            Toast.makeText(this, "Geçerli bir noktaya dokunun", Toast.LENGTH_SHORT).show();
            return;
        }

        // Önce varsa önceki preview'ü kalıcı say
        commitActivePreviewIfNeeded();

        Bitmap base = workingBitmap;
        if (base == null) return;

        saveStateForUndo(base);

        previewBaseBitmap = base.copy(Bitmap.Config.ARGB_8888, true);
        lastTappedX = bitmapX;
        lastTappedY = bitmapY;
        hasActivePreview = true;

        tvInfo.setText("Canlı önizleme hazırlanıyor...");
        renderPreviewFromActiveState();
    }

    private void updateLivePreviewForCurrentSelection() {
        if (!hasActivePreview) return;
        renderPreviewFromActiveState();
    }

    private void renderPreviewFromActiveState() {
        Bitmap baseBitmap = previewBaseBitmap;
        if (baseBitmap == null) return;
        if (!hasActivePreview) return;
        if (lastTappedX < 0 || lastTappedY < 0) return;

        isProcessing = true;
        updateUndoButtonState();

        final int generation = ++previewGeneration;
        final int startX = lastTappedX;
        final int startY = lastTappedY;
        final int currentTolerance = tolerance;
        final Bitmap baseCopy = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);

        executor.execute(() -> {
            Bitmap resultBitmap;

            try {
                int targetColor = baseCopy.getPixel(startX, startY);

                resultBitmap = removeConnectedRegionByColor(
                        baseCopy,
                        startX,
                        startY,
                        targetColor,
                        currentTolerance
                );
            } catch (Exception e) {
                e.printStackTrace();
                resultBitmap = null;
            }

            Bitmap finalResultBitmap = resultBitmap;

            runOnUiThread(() -> {
                // Eğer kullanıcı bu sırada yeni preview başlattıysa eski sonucu görmezden gel
                if (generation != previewGeneration) {
                    return;
                }

                isProcessing = false;
                updateUndoButtonState();

                if (finalResultBitmap == null) {
                    tvInfo.setText("Önizleme oluşturulamadı.");
                    return;
                }

                workingBitmap = finalResultBitmap;
                imageView.setBitmap(workingBitmap);

                tvInfo.setText("Canlı önizleme aktif. Tolerans: " + currentTolerance);
            });
        });
    }

    private void commitActivePreviewIfNeeded() {
        if (!hasActivePreview) return;

        previewBaseBitmap = null;
        hasActivePreview = false;
        lastTappedX = -1;
        lastTappedY = -1;
    }

    private void clearActivePreview() {
        previewGeneration++;
        previewBaseBitmap = null;
        hasActivePreview = false;
        lastTappedX = -1;
        lastTappedY = -1;
    }

    private void saveStateForUndo(Bitmap bitmap) {
        Bitmap snapshot = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        if (undoStack.size() >= maxUndoCount) {
            undoStack.removeFirst();
        }

        undoStack.addLast(snapshot);
        updateUndoButtonState();
    }

    private void undoLastAction() {
        previewGeneration++;

        if (undoStack.isEmpty()) {
            Toast.makeText(this, "Geri alınacak işlem yok", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap previousBitmap = undoStack.removeLast();
        workingBitmap = previousBitmap;
        imageView.setBitmap(workingBitmap);

        clearActivePreview();

        isProcessing = false;
        tvInfo.setText("Son işlem geri alındı.");
        updateUndoButtonState();
    }

    private void clearUndoHistory() {
        undoStack.clear();
        updateUndoButtonState();
    }

    private void updateUndoButtonState() {
        btnUndo.setEnabled(!undoStack.isEmpty() && !isProcessing);
    }

    private Bitmap removeConnectedRegionByColor(
            Bitmap source,
            int startX,
            int startY,
            int targetColor,
            int tolerance
    ) {
        Bitmap result = source.copy(Bitmap.Config.ARGB_8888, true);

        int width = result.getWidth();
        int height = result.getHeight();

        int[] pixels = new int[width * height];
        result.getPixels(pixels, 0, width, 0, 0, width, height);

        boolean[] visited = new boolean[width * height];
        ArrayDeque<IntPoint> queue = new ArrayDeque<>();

        queue.add(new IntPoint(startX, startY));

        int targetR = Color.red(targetColor);
        int targetG = Color.green(targetColor);
        int targetB = Color.blue(targetColor);

        while (!queue.isEmpty()) {
            IntPoint p = queue.removeFirst();
            int x = p.x;
            int y = p.y;

            if (x < 0 || x >= width || y < 0 || y >= height) continue;

            int index = y * width + x;
            if (visited[index]) continue;
            visited[index] = true;

            int pixel = pixels[index];
            if (Color.alpha(pixel) == 0) continue;

            int r = Color.red(pixel);
            int g = Color.green(pixel);
            int b = Color.blue(pixel);

            double distance = colorDistance(r, g, b, targetR, targetG, targetB);

            if (distance <= tolerance) {
                pixels[index] = Color.TRANSPARENT;

                queue.add(new IntPoint(x + 1, y));
                queue.add(new IntPoint(x - 1, y));
                queue.add(new IntPoint(x, y + 1));
                queue.add(new IntPoint(x, y - 1));
            }
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private double colorDistance(
            int r1, int g1, int b1,
            int r2, int g2, int b2
    ) {
        double dr = r1 - r2;
        double dg = g1 - g2;
        double db = b1 - b2;
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private void resetImage() {
        previewGeneration++;

        if (originalBitmap == null) return;

        workingBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setBitmap(workingBitmap);
        imageView.forceResetZoom();

        clearUndoHistory();
        clearActivePreview();

        isProcessing = false;
        tvInfo.setText("Görsel sıfırlandı. Baştan işlem yapabilirsin.");
        updateUndoButtonState();
    }

    private void saveTransparentImage() {
        Bitmap bitmap = workingBitmap;
        if (bitmap == null) {
            Toast.makeText(this, "Kaydedilecek görsel yok", Toast.LENGTH_SHORT).show();
            return;
        }

        isProcessing = true;
        updateUndoButtonState();
        tvInfo.setText("Görsel kaydediliyor...");

        Bitmap saveBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        executor.execute(() -> {
            boolean success = saveBitmapAsPng(saveBitmap);

            runOnUiThread(() -> {
                isProcessing = false;
                updateUndoButtonState();

                if (success) {
                    tvInfo.setText("Arka plansız görsel PNG olarak kaydedildi.");
                    Toast.makeText(MainActivity.this, "Kaydetme başarılı", Toast.LENGTH_SHORT).show();
                } else {
                    tvInfo.setText("Kaydetme başarısız oldu.");
                    Toast.makeText(MainActivity.this, "Kaydetme başarısız", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean saveBitmapAsPng(Bitmap bitmap) {
        try {
            String fileName = "bg_removed_" + System.currentTimeMillis() + ".png";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ColorRemover");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }

            Uri uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            );

            if (uri == null) return false;

            OutputStream outputStream = null;
            try {
                outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream == null) return false;

                boolean result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, updateValues, null, null);
                }

                return result;
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private static class IntPoint {
        final int x;
        final int y;

        IntPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}