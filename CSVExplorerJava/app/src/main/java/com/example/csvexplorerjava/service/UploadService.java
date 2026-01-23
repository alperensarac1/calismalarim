package com.example.csvexplorerjava.service;


import android.content.ContentResolver;
import android.net.Uri;

import com.example.csvexplorerjava.model.UploadResult;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UploadService {

    private UploadService() {}

    public static UploadResult uploadCsv(ContentResolver contentResolver, Uri fileUri, String endpointUrl) {
        String boundary = "----DynamicCsvBoundary" + System.currentTimeMillis();
        String lineEnd = "\r\n";

        HttpURLConnection conn = null;

        try {
            URL url = new URL(endpointUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("Connection", "Keep-Alive");

            DataOutputStream output = new DataOutputStream(conn.getOutputStream());

            // Part: file
            output.writeBytes("--" + boundary + lineEnd);
            output.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"data.csv\"" + lineEnd);
            output.writeBytes("Content-Type: text/csv" + lineEnd);
            output.writeBytes(lineEnd);

            InputStream input = contentResolver.openInputStream(fileUri);
            if (input == null) throw new IllegalArgumentException("Dosya açılamadı");

            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            input.close();

            output.writeBytes(lineEnd);
            output.writeBytes("--" + boundary + "--" + lineEnd);
            output.flush();
            output.close();

            int code = conn.getResponseCode();
            InputStream bodyStream = (code >= 200 && code <= 299) ? conn.getInputStream() : conn.getErrorStream();
            String body = bodyStream != null ? new String(bodyStream.readAllBytes()) : "";

            if (code < 200 || code > 299) {
                return new UploadResult(false, null, "HTTP " + code + ": " + body);
            }

            JSONObject json = new JSONObject(body);
            boolean ok = json.optBoolean("ok", false);
            if (!ok) return new UploadResult(false, null, json.optString("error", "unknown error"));

            String downloadUrl = json.optString("download_url", null);
            return new UploadResult(true, downloadUrl, null);

        } catch (Exception e) {
            return new UploadResult(false, null, e.getMessage() != null ? e.getMessage() : "upload error");
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}

