package com.example.csvexplorerjava.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csvexplorerjava.R;
import com.example.csvexplorerjava.model.FieldItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.VH> {

    private final Context context;
    private List<FieldItem> items = new ArrayList<>();
    private String query = "";

    public FieldAdapter(Context context) {
        this.context = context;
    }

    public void submit(List<FieldItem> list, String searchQuery) {
        items = list != null ? list : new ArrayList<>();
        query = searchQuery != null ? searchQuery.trim() : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field, parent, false);
        return new VH(v);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FieldItem item = items.get(position);

        holder.tvKey.setText(highlight(item.key, query));
        holder.tvValue.setText(highlight(item.value, query));

        holder.btnCopy.setOnClickListener(v -> {
            copyToClipboard("field_value", item.value);
            Toast.makeText(context, "Copied: " + item.key, Toast.LENGTH_SHORT).show();
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvKey;
        final TextView tvValue;
        final TextView btnCopy;

        VH(@NonNull View itemView) {
            super(itemView);
            tvKey = itemView.findViewById(R.id.tvKey);
            tvValue = itemView.findViewById(R.id.tvValue);
            btnCopy = itemView.findViewById(R.id.btnCopy);
        }
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    private CharSequence highlight(String text, String q) {
        if (q == null || q.isBlank()) return text;

        String lowerText = text.toLowerCase(Locale.getDefault());
        String lowerQ = q.toLowerCase(Locale.getDefault());

        int start = lowerText.indexOf(lowerQ);
        if (start < 0) return text;

        int end = Math.min(start + q.length(), text.length());
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new BackgroundColorSpan(0x33FFF59D), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }
}

