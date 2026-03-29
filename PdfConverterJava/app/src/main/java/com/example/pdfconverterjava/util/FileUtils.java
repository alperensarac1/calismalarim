package com.example.pdfconverterjava.util;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    /**
     * Uri üzerinden dosya adını almaya çalışır.
     * Eğer sistem gerçek adı vermezse zaman bazlı bir isim üretir.
     */
    public static String getFileName(Context context, Uri uri) {
        String fileName = "file_" + System.currentTimeMillis();

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    String value = cursor.getString(nameIndex);
                    if (value != null && !value.trim().isEmpty()) {
                        fileName = value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return fileName;
    }

    /**
     * Uri içeriğini uygulamanın cache klasörüne kopyalar.
     * Sonra bu geçici dosyayı File olarak geri döner.
     */
    public static File copyUriToFile(Context context, Uri uri) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            String fileName = getFileName(context, uri);
            File tempFile = new File(context.getCacheDir(), fileName);

            inputStream = context.getContentResolver().openInputStream(uri);
            outputStream = new FileOutputStream(tempFile);

            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            return tempFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (outputStream != null) outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Birden fazla Uri gelirse hepsini File listesine çevirir.
     */
    public static List<File> copyUrisToFiles(Context context, List<Uri> uris) {
        List<File> files = new ArrayList<>();

        for (Uri uri : uris) {
            File file = copyUriToFile(context, uri);
            if (file != null) {
                files.add(file);
            }
        }

        return files;
    }
}
