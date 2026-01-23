package com.example.csvexplorerjava.usecases;

import android.content.ContentResolver;
import android.net.Uri;

import com.example.csvexplorerjava.entity.RowEntity;
import com.example.csvexplorerjava.model.CsvImportResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DynamicCSVImporter {

    private DynamicCSVImporter() {}

    public static CsvImportResult importCsv(ContentResolver contentResolver, Uri uri) throws Exception {
        InputStream input = contentResolver.openInputStream(uri);
        if (input == null) throw new IllegalArgumentException("CSV açılamadı");

        BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        String headerLine = br.readLine();
        if (headerLine == null) return new CsvImportResult(new ArrayList<>(), new ArrayList<>());

        List<String> headers = new ArrayList<>();
        for (String h : splitCsvLine(headerLine)) {
            String t = h.trim();
            if (!t.isEmpty()) headers.add(t);
        }

        ArrayList<RowEntity> out = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String raw = line.trim();
            if (raw.isEmpty()) continue;

            List<String> values = splitCsvLine(raw);
            JSONObject obj = new JSONObject();

            for (int i = 0; i < headers.size(); i++) {
                String key = headers.get(i);
                String value = i < values.size() ? values.get(i).trim() : null;
                if (value != null && !value.isEmpty()) obj.put(key, value);
            }

            String externalId = guessExternalId(headers, obj);
            out.add(new RowEntity(externalId, obj.toString()));
        }

        br.close();
        input.close();

        return new CsvImportResult(headers, out);
    }

    private static String guessExternalId(List<String> headers, JSONObject obj) {
        List<String> candidates = Arrays.asList("id", "ID", "Id", "user_id", "uid", "pk");
        for (String c : candidates) {
            if (headers.contains(c) && obj.has(c)) {
                String v = obj.optString(c, null);
                if (v != null && !v.trim().isEmpty()) return v;
            }
        }
        return null;
    }

    private static List<String> splitCsvLine(String line) {
        ArrayList<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                Character next = (i + 1 < line.length()) ? line.charAt(i + 1) : null;
                if (inQuotes && next != null && next == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }
}

