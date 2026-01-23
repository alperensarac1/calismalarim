package com.example.csvexplorerjava.adapter;


import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.csvexplorerjava.R;
import com.example.csvexplorerjava.entity.RowEntity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RowAdapter extends BaseAdapter {

    public interface OnRowClick {
        void onClick(RowEntity item);
    }

    private final Context ctx;
    private final OnRowClick onRowClick;

    private List<RowEntity> items = new ArrayList<>();
    private List<String> headers = new ArrayList<>();

    private String query = "";
    private String selectedColumn = "ALL_COLUMNS";

    public RowAdapter(Context ctx, OnRowClick onRowClick) {
        this.ctx = ctx;
        this.onRowClick = onRowClick;
    }

    public void submit(List<RowEntity> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setHeaders(List<String> h) {
        headers = h != null ? h : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setHighlight(String q, String selected) {
        query = q != null ? q.trim() : "";
        selectedColumn = selected != null ? selected : "ALL_COLUMNS";
        notifyDataSetChanged();
    }

    @Override public int getCount() { return items.size(); }
    @Override public RowEntity getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView != null ? convertView : LayoutInflater.from(ctx).inflate(R.layout.row_item, parent, false);

        TextView tvTitle = v.findViewById(R.id.tvTitle);
        TextView tvSubtitle = v.findViewById(R.id.tvSubtitle);
        ChipGroup chipGroup = v.findViewById(R.id.chipGroup);

        RowEntity item = getItem(position);
        JSONObject obj;
        try { obj = new JSONObject(item.getDataJson()); }
        catch (Exception e) { obj = new JSONObject(); }

        String id = obj.optString("id", "");
        String first = obj.optString("first_name", obj.optString("firstname", ""));
        String last  = obj.optString("last_name", obj.optString("lastname", ""));
        String name = (first + " " + last).trim();

        String titleText;
        if (!id.isBlank() && !name.isBlank()) titleText = "#" + id + "  " + name;
        else if (!id.isBlank()) titleText = "#" + id;
        else if (!name.isBlank()) titleText = name;
        else titleText = "Row " + (position + 1);

        String lastSeen = obj.optString("last_seen", "");
        String country = obj.optString("country_title", "");
        String city = obj.optString("city_title", "");

        String subtitleText;
        if (!lastSeen.isBlank() && (!country.isBlank() || !city.isBlank())) {
            String place = String.join(" / ", filterBlank(country, city));
            subtitleText = "Last seen: " + lastSeen + " • " + place;
        } else if (!lastSeen.isBlank()) {
            subtitleText = "Last seen: " + lastSeen;
        } else if (!country.isBlank() || !city.isBlank()) {
            subtitleText = String.join(" / ", filterBlank(country, city));
        } else {
            subtitleText = buildFallbackSubtitle(obj);
        }

        tvTitle.setText(highlightAll(titleText, query));
        tvSubtitle.setText(highlightAll(subtitleText, query));

        chipGroup.removeAllViews();

        List<Pair> chips = buildChipPairs(obj);
        for (Pair p : chips) {
            Chip chip = new Chip(ctx);
            chip.setText(highlightAll(p.k + ": " + p.v, query));
            chip.setClickable(false);
            chip.setCheckable(false);
            chip.setFocusable(false);
            chip.setEnabled(true);
            chipGroup.addView(chip);
        }

        v.setOnClickListener(view -> {
            if (onRowClick != null) onRowClick.onClick(item);
        });

        return v;
    }

    private List<String> filterBlank(String a, String b) {
        ArrayList<String> out = new ArrayList<>();
        if (a != null && !a.isBlank()) out.add(a);
        if (b != null && !b.isBlank()) out.add(b);
        return out;
    }

    private String buildFallbackSubtitle(JSONObject obj) {
        ArrayList<String> keys = new ArrayList<>();
        if (headers != null && !headers.isEmpty()) keys.addAll(headers);
        else {
            for (java.util.Iterator<String> it = obj.keys(); it.hasNext();) keys.add(it.next());
        }

        ArrayList<String> pairs = new ArrayList<>();
        for (String k : keys) {
            String val = obj.optString(k, "");
            if (val != null && !val.isBlank()) {
                pairs.add(k + ": " + val);
                if (pairs.size() >= 2) break;
            }
        }

        if (pairs.isEmpty()) return "Tap to view details";
        if (pairs.size() == 1) return pairs.get(0);
        return pairs.get(0) + " • " + pairs.get(1);
    }

    private static class Pair {
        final String k;
        final String v;
        Pair(String k, String v) { this.k = k; this.v = v; }
    }

    private List<Pair> buildChipPairs(JSONObject obj) {
        ArrayList<String> keys = new ArrayList<>();
        if (headers != null && !headers.isEmpty()) keys.addAll(headers);
        else {
            for (java.util.Iterator<String> it = obj.keys(); it.hasNext();) keys.add(it.next());
        }

        ArrayList<String> ordered = new ArrayList<>();
        if (!"ALL_COLUMNS".equals(selectedColumn)) {
            ordered.add(selectedColumn);
            for (String k : keys) if (!k.equals(selectedColumn)) ordered.add(k);
        } else {
            ordered.addAll(keys);
        }

        ArrayList<Pair> out = new ArrayList<>();
        for (String k : ordered) {
            String v = obj.optString(k, "");
            if (v != null && !v.isBlank()) out.add(new Pair(k, v));
            if (out.size() >= 4) break;
        }

        if (out.isEmpty()) {
            for (int i = 0; i < Math.min(2, ordered.size()); i++) {
                String k = ordered.get(i);
                out.add(new Pair(k, obj.optString(k, "-")));
            }
        }

        return out;
    }

    private CharSequence highlightAll(String text, String q) {
        if (q == null || q.isBlank()) return text;

        String lowerText = text.toLowerCase(Locale.getDefault());
        String lowerQ = q.toLowerCase(Locale.getDefault());
        int start = lowerText.indexOf(lowerQ);
        if (start < 0) return text;

        SpannableString ss = new SpannableString(text);
        while (start >= 0) {
            int end = Math.min(start + lowerQ.length(), text.length());
            ss.setSpan(new BackgroundColorSpan(0x33FFF59D), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = lowerText.indexOf(lowerQ, end);
        }
        return ss;
    }
}

