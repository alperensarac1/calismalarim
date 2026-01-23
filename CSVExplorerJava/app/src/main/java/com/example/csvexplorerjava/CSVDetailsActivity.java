package com.example.csvexplorerjava;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.csvexplorerjava.adapter.FieldAdapter;
import com.example.csvexplorerjava.databinding.ActivityCsvdetailsBinding;
import com.example.csvexplorerjava.model.FieldItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CSVDetailsActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    public static final String EXTRA_JSON = "extra_json";
    public static final String EXTRA_HEADERS = "extra_headers";

    private ActivityCsvdetailsBinding binding;

    private FieldAdapter adapter;
    private List<FieldItem> allFields = new ArrayList<>();

    private String rowJson = "";
    private List<String> headers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCsvdetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String json = getIntent().getStringExtra(EXTRA_JSON);
        rowJson = json != null ? json : "";

        ArrayList<String> hdr = getIntent().getStringArrayListExtra(EXTRA_HEADERS);
        headers = hdr != null ? hdr : Collections.emptyList();

        JSONObject obj;
        try { obj = new JSONObject(rowJson); }
        catch (Exception e) { obj = new JSONObject(); }

        String first = obj.optString("first_name", obj.optString("firstname", ""));
        String last  = obj.optString("last_name", obj.optString("lastname", ""));
        String id    = obj.optString("id", "");

        String title;
        if (!id.isBlank() && (!first.isBlank() || !last.isBlank())) title = "#" + id + "  " + first + " " + last;
        else if (!id.isBlank()) title = "#" + id;
        else if (!first.isBlank() || !last.isBlank()) title = first + " " + last;
        else title = "CSV Row Details";

        binding.tvTitle.setText(title.trim());

        adapter = new FieldAdapter(this);
        binding.rvFields.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFields.setAdapter(adapter);

        allFields = buildFields(headers, obj);
        binding.tvCount.setText(allFields.size() + " fields");
        adapter.submit(allFields, "");

        // implements TextWatcher
        binding.etSearchDetails.addTextChangedListener(this);

        // implements OnClickListener
        binding.btnCopyJson.setOnClickListener(this);
        binding.btnCopyCsvRow.setOnClickListener(this);
        binding.btnBack.setOnClickListener(this);
    }

    // TextWatcher
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void afterTextChanged(Editable s) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String q = s != null ? s.toString().trim() : "";
        List<FieldItem> filtered = filterFields(allFields, q);
        binding.tvCount.setText(filtered.size() + " matches");
        adapter.submit(filtered, q);
    }

    // OnClickListener
    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();

        if (id == R.id.btnCopyJson) {
            copyToClipboard("row_json", rowJson);
            Toast.makeText(this, "Copied JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        if (id == R.id.btnCopyCsvRow) {
            try {
                JSONObject obj = new JSONObject(rowJson);
                String csv = buildCsvRow(headers, obj);
                copyToClipboard("row_csv", csv);
                Toast.makeText(this, "Copied CSV row", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Invalid JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (id == R.id.btnBack) {
            finish();
        }
    }

    private List<FieldItem> filterFields(List<FieldItem> list, String q) {
        if (q == null || q.isBlank()) return list;
        String qq = q.toLowerCase(Locale.getDefault());

        ArrayList<FieldItem> out = new ArrayList<>();
        for (FieldItem it : list) {
            if (it.key.toLowerCase(Locale.getDefault()).contains(qq) ||
                    it.value.toLowerCase(Locale.getDefault()).contains(qq)) {
                out.add(it);
            }
        }
        return out;
    }

    private List<FieldItem> buildFields(List<String> headers, JSONObject obj) {
        ArrayList<FieldItem> out = new ArrayList<>();

        if (headers != null && !headers.isEmpty()) {
            for (String h : headers) {
                String v = obj.optString(h, "");
                out.add(new FieldItem(h, (v == null || v.isBlank()) ? "-" : v));
            }

            ArrayList<String> extras = new ArrayList<>();
            for (java.util.Iterator<String> it = obj.keys(); it.hasNext();) {
                String k = it.next();
                if (!headers.contains(k)) extras.add(k);
            }
            Collections.sort(extras);
            for (String k : extras) {
                String v = obj.optString(k, "");
                out.add(new FieldItem(k, (v == null || v.isBlank()) ? "-" : v));
            }
        } else {
            ArrayList<String> keys = new ArrayList<>();
            for (java.util.Iterator<String> it = obj.keys(); it.hasNext();) keys.add(it.next());
            Collections.sort(keys);

            for (String k : keys) {
                String v = obj.optString(k, "");
                out.add(new FieldItem(k, (v == null || v.isBlank()) ? "-" : v));
            }
        }

        return out;
    }

    private String buildCsvRow(List<String> headers, JSONObject obj) {
        if (headers == null || headers.isEmpty()) return obj.toString();

        String headerLine = String.join(",", headers);

        ArrayList<String> vals = new ArrayList<>();
        for (String h : headers) {
            vals.add(esc(obj.optString(h, "")));
        }
        String rowLine = String.join(",", vals);

        return headerLine + "\n" + rowLine;
    }

    private String esc(String value) {
        if (value == null) value = "";
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String v = value.replace("\"", "\"\"");
        if (needsQuotes) v = "\"" + v + "\"";
        return v;
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText(label, text));
    }
}
