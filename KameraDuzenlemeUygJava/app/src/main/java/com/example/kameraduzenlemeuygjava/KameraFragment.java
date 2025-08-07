package com.example.kameraduzenlemeuygjava;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.kameraduzenlemeuygjava.databinding.FragmentKameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;


public class KameraFragment extends Fragment {

    private FragmentKameraBinding binding;
    private ImageCapture imageCapture;

    private final String[] cameraPermissions = new String[]{
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private final int permissionRequestCode = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentKameraBinding.inflate(inflater, container, false);

        if (arePermissionsGranted()) {
            setupCamera();
        } else {
            requestPermissions(cameraPermissions, permissionRequestCode);
        }

        binding.takePhotoButton.setOnClickListener(v -> takePhoto());

        binding.addTextButton.setOnClickListener(v -> showTextInputDialog());

        binding.saveScreenshotButton.setOnClickListener(v -> saveScreenshot());

        return binding.getRoot();
    }

    private boolean arePermissionsGranted() {
        Context context = requireContext();
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        File photoFile = new File(requireContext().getExternalCacheDir(),
                System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        showPhoto(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                    }
                });
    }

    private void showPhoto(File photoFile) {
        binding.previewView.setVisibility(View.GONE);
        binding.imageView.setVisibility(View.VISIBLE);

        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        try {
            ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateBitmap(bitmap, 90f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateBitmap(bitmap, 180f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateBitmap(bitmap, 270f);
                    break;
                default:
                    rotatedBitmap = bitmap;
            }

            binding.imageView.setImageBitmap(rotatedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void showTextInputDialog() {
        EditText editText = new EditText(requireContext());
        new AlertDialog.Builder(requireContext())
                .setTitle("Yazı Ekle")
                .setMessage("Eklemek istediğiniz yazıyı girin")
                .setView(editText)
                .setPositiveButton("Ekle", (dialog, which) -> {
                    String text = editText.getText().toString();
                    addTextToImage(text);
                })
                .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addTextToImage(String text) {
        Drawable drawable = binding.imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitmap);

                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setTextSize(80f);
                paint.setAntiAlias(true);
                paint.setTextAlign(Paint.Align.CENTER);

                canvas.drawText(text, 100f, 100f, paint);

                binding.imageView.setImageBitmap(mutableBitmap);
            }
        }
    }

    private void saveScreenshot() {
        binding.imageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(binding.imageView.getDrawingCache());
        binding.imageView.setDrawingCacheEnabled(false);

        if (bitmap != null) {
            try {
                ContentResolver contentResolver = requireContext().getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "screenshot_" + System.currentTimeMillis() + ".jpg");
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (uri != null) {
                    OutputStream outputStream = contentResolver.openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                    Log.d("Screenshot", "Ekran görüntüsü kaydedildi: " + uri);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("Screenshot", "Bitmap oluşturulamadı!");
        }
    }

    // İzin yanıtı
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permissionRequestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(requireContext())
                .setMessage("Kamera ve depolama izinlerini vermeniz gerekiyor. Ayarlara gitmek ister misiniz?")
                .setCancelable(false)
                .setPositiveButton("Evet", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Hayır", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
