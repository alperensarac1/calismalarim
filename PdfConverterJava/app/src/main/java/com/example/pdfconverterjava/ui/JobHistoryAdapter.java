package com.example.pdfconverterjava.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdfconverterjava.R;
import com.example.pdfconverterjava.data.model.JobItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobHistoryAdapter extends RecyclerView.Adapter<JobHistoryAdapter.JobViewHolder> {

    private final List<JobItem> items;

    // Her satır için aktif indirme id'si tutuyoruz
    private final Map<Integer, Long> activeDownloads = new HashMap<>();

    // Progress kontrolü için main thread handler
    private final Handler handler = new Handler(Looper.getMainLooper());

    public JobHistoryAdapter(List<JobItem> items) {
        this.items = items;
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobType;
        TextView tvJobStatus;
        TextView tvCreatedAt;
        TextView tvErrorMessage;
        TextView tvDownloadPercent;
        Button btnOpenResult;
        Button btnDownload;
        ProgressBar progressDownload;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);

            tvJobType = itemView.findViewById(R.id.tvJobType);
            tvJobStatus = itemView.findViewById(R.id.tvJobStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvErrorMessage = itemView.findViewById(R.id.tvErrorMessage);
            tvDownloadPercent = itemView.findViewById(R.id.tvDownloadPercent);
            btnOpenResult = itemView.findViewById(R.id.btnOpenResult);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            progressDownload = itemView.findViewById(R.id.progressDownload);
        }
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobItem item = items.get(position);

        holder.tvJobType.setText("İşlem: " + mapJobType(item.getJob_type()));
        holder.tvJobStatus.setText("Durum: " + mapStatus(item.getStatus()));
        holder.tvCreatedAt.setText("Tarih: " + safe(item.getCreated_at()));

        if (item.getError_message() != null && !item.getError_message().trim().isEmpty()) {
            holder.tvErrorMessage.setVisibility(View.VISIBLE);
            holder.tvErrorMessage.setText("Hata: " + item.getError_message());
        } else {
            holder.tvErrorMessage.setVisibility(View.GONE);
        }

        if (item.getResult_file_url() != null && !item.getResult_file_url().trim().isEmpty()) {
            holder.btnOpenResult.setVisibility(View.VISIBLE);
            holder.btnDownload.setVisibility(View.VISIBLE);

            holder.btnOpenResult.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getResult_file_url()));
                context.startActivity(intent);
            });

            holder.btnDownload.setOnClickListener(v -> {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    startDownloadWithProgress(
                            holder.itemView.getContext(),
                            item.getResult_file_url(),
                            holder,
                            adapterPosition
                    );
                }
            });

            // Eğer bu satır için aktif indirme yoksa görünümü sıfırla
            if (!activeDownloads.containsKey(position)) {
                holder.progressDownload.setVisibility(View.GONE);
                holder.tvDownloadPercent.setVisibility(View.GONE);
                holder.progressDownload.setProgress(0);
                holder.tvDownloadPercent.setText("0%");
                holder.btnDownload.setEnabled(true);
                holder.btnDownload.setText("İndir");
            }

        } else {
            holder.btnOpenResult.setVisibility(View.GONE);
            holder.btnDownload.setVisibility(View.GONE);
            holder.progressDownload.setVisibility(View.GONE);
            holder.tvDownloadPercent.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<JobItem> newItems) {
        items.clear();
        items.addAll(newItems);
        activeDownloads.clear();
        notifyDataSetChanged();
    }

    private void startDownloadWithProgress(
            Context context,
            String url,
            JobViewHolder holder,
            int position
    ) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(fileName);
        request.setDescription("Dosya indiriliyor...");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
        );
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE
        );

        DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        long downloadId = downloadManager.enqueue(request);
        activeDownloads.put(position, downloadId);

        holder.progressDownload.setVisibility(View.VISIBLE);
        holder.tvDownloadPercent.setVisibility(View.VISIBLE);
        holder.progressDownload.setProgress(0);
        holder.tvDownloadPercent.setText("0%");
        holder.btnDownload.setEnabled(false);
        holder.btnDownload.setText("İndiriliyor...");

        trackDownloadProgress(context, downloadId, holder, position);
    }

    private void trackDownloadProgress(
            Context context,
            long downloadId,
            JobViewHolder holder,
            int position
    ) {
        DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;

                try {
                    cursor = downloadManager.query(query);

                    if (cursor != null && cursor.moveToFirst()) {
                        int downloadedIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                        int totalIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                        int statusIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

                        long downloadedBytes = cursor.getLong(downloadedIndex);
                        long totalBytes = cursor.getLong(totalIndex);
                        int status = cursor.getInt(statusIndex);

                        if (totalBytes > 0) {
                            int progress = (int) ((downloadedBytes * 100L) / totalBytes);
                            holder.progressDownload.setProgress(progress);
                            holder.tvDownloadPercent.setText(progress + "%");
                        }

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            holder.progressDownload.setProgress(100);
                            holder.tvDownloadPercent.setText("100%");
                            holder.btnDownload.setEnabled(true);
                            holder.btnDownload.setText("Tekrar İndir");

                            handler.postDelayed(() -> {
                                holder.progressDownload.setVisibility(View.GONE);
                                holder.tvDownloadPercent.setVisibility(View.GONE);
                            }, 1200);

                            activeDownloads.remove(position);

                        } else if (status == DownloadManager.STATUS_FAILED) {
                            holder.btnDownload.setEnabled(true);
                            holder.btnDownload.setText("Tekrar Dene");
                            holder.tvDownloadPercent.setVisibility(View.VISIBLE);
                            holder.tvDownloadPercent.setText("Başarısız");
                            activeDownloads.remove(position);

                        } else if (status == DownloadManager.STATUS_RUNNING
                                || status == DownloadManager.STATUS_PENDING
                                || status == DownloadManager.STATUS_PAUSED) {
                            handler.postDelayed(this, 500);
                        }
                    } else {
                        holder.btnDownload.setEnabled(true);
                        holder.btnDownload.setText("İndir");
                        activeDownloads.remove(position);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.btnDownload.setEnabled(true);
                    holder.btnDownload.setText("İndir");
                    activeDownloads.remove(position);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        };

        handler.post(progressRunnable);
    }

    private String mapJobType(String jobType) {
        if ("jpg_to_pdf".equals(jobType)) return "JPG to PDF";
        if ("pdf_to_word".equals(jobType)) return "PDF to Word";
        if ("word_to_pdf".equals(jobType)) return "Word to PDF";
        if ("pdf_merge".equals(jobType)) return "PDF Birleştirme";
        return safe(jobType);
    }

    private String mapStatus(String status) {
        if ("waiting".equals(status)) return "Bekliyor";
        if ("processing".equals(status)) return "İşleniyor";
        if ("done".equals(status)) return "Tamamlandı";
        if ("failed".equals(status)) return "Başarısız";
        return safe(status);
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
