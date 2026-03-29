package com.example.pdfconverterkotlin.ui.adapter
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfconverterkotlin.R
import com.example.pdfconverterkotlin.data.model.JobItem

class JobHistoryAdapter(
    private val items: MutableList<JobItem> = mutableListOf()
) : RecyclerView.Adapter<JobHistoryAdapter.JobViewHolder>() {

    private val activeDownloads = mutableMapOf<Int, Long>()

    private val handler = Handler(Looper.getMainLooper())

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJobType: TextView = itemView.findViewById(R.id.tvJobType)
        val tvJobStatus: TextView = itemView.findViewById(R.id.tvJobStatus)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        val tvErrorMessage: TextView = itemView.findViewById(R.id.tvErrorMessage)
        val btnOpenResult: Button = itemView.findViewById(R.id.btnOpenResult)
        val btnDownload: Button = itemView.findViewById(R.id.btnDownload)
        val progressDownload: ProgressBar = itemView.findViewById(R.id.progressDownload)
        val tvDownloadPercent: TextView = itemView.findViewById(R.id.tvDownloadPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val item = items[position]

        holder.tvJobType.text = "İşlem: ${mapJobType(item.job_type)}"
        holder.tvJobStatus.text = "Durum: ${mapStatus(item.status)}"
        holder.tvCreatedAt.text = "Tarih: ${item.created_at ?: "-"}"

        if (!item.error_message.isNullOrBlank()) {
            holder.tvErrorMessage.visibility = View.VISIBLE
            holder.tvErrorMessage.text = "Hata: ${item.error_message}"
        } else {
            holder.tvErrorMessage.visibility = View.GONE
        }

        if (!item.result_file_url.isNullOrBlank()) {
            holder.btnOpenResult.visibility = View.VISIBLE
            holder.btnDownload.visibility = View.VISIBLE

            holder.btnOpenResult.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.result_file_url))
                context.startActivity(intent)
            }

            holder.btnDownload.setOnClickListener {
                val context = holder.itemView.context
                startDownloadWithProgress(
                    context = context,
                    url = item.result_file_url!!,
                    holder = holder,
                    position = holder.bindingAdapterPosition
                )
            }

            // Eğer bu satır için aktif indirme yoksa default görünüm
            if (!activeDownloads.containsKey(position)) {
                holder.progressDownload.visibility = View.GONE
                holder.tvDownloadPercent.visibility = View.GONE
                holder.progressDownload.progress = 0
                holder.tvDownloadPercent.text = "0%"
                holder.btnDownload.isEnabled = true
                holder.btnDownload.text = "İndir"
            }

        } else {
            holder.btnOpenResult.visibility = View.GONE
            holder.btnDownload.visibility = View.GONE
            holder.progressDownload.visibility = View.GONE
            holder.tvDownloadPercent.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<JobItem>) {
        items.clear()
        items.addAll(newItems)
        activeDownloads.clear()
        notifyDataSetChanged()
    }

    private fun startDownloadWithProgress(
        context: Context,
        url: String,
        holder: JobViewHolder,
        position: Int
    ) {
        val fileName = url.substringAfterLast("/")

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setDescription("Dosya indiriliyor...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
        }

        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadId = downloadManager.enqueue(request)
        activeDownloads[position] = downloadId

        holder.progressDownload.visibility = View.VISIBLE
        holder.tvDownloadPercent.visibility = View.VISIBLE
        holder.progressDownload.progress = 0
        holder.tvDownloadPercent.text = "0%"
        holder.btnDownload.isEnabled = false
        holder.btnDownload.text = "İndiriliyor..."

        trackDownloadProgress(
            context = context,
            downloadId = downloadId,
            holder = holder,
            position = position
        )
    }

    private fun trackDownloadProgress(
        context: Context,
        downloadId: Long,
        holder: JobViewHolder,
        position: Int
    ) {
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val query = DownloadManager.Query().setFilterById(downloadId)

        val progressRunnable = object : Runnable {
            override fun run() {
                var cursor: Cursor? = null

                try {
                    cursor = downloadManager.query(query)

                    if (cursor != null && cursor.moveToFirst()) {
                        val bytesDownloadedIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val statusIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                        val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                        val bytesTotal = cursor.getLong(bytesTotalIndex)
                        val status = cursor.getInt(statusIndex)

                        if (bytesTotal > 0) {
                            val progress = ((bytesDownloaded * 100L) / bytesTotal).toInt()
                            holder.progressDownload.progress = progress
                            holder.tvDownloadPercent.text = "$progress%"
                        }

                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                holder.progressDownload.progress = 100
                                holder.tvDownloadPercent.text = "100%"
                                holder.btnDownload.isEnabled = true
                                holder.btnDownload.text = "Tekrar İndir"

                                handler.postDelayed({
                                    holder.progressDownload.visibility = View.GONE
                                    holder.tvDownloadPercent.visibility = View.GONE
                                }, 1200)

                                activeDownloads.remove(position)
                            }

                            DownloadManager.STATUS_FAILED -> {
                                holder.btnDownload.isEnabled = true
                                holder.btnDownload.text = "Tekrar Dene"
                                holder.tvDownloadPercent.text = "Başarısız"
                                activeDownloads.remove(position)
                            }

                            DownloadManager.STATUS_RUNNING,
                            DownloadManager.STATUS_PENDING,
                            DownloadManager.STATUS_PAUSED -> {
                                handler.postDelayed(this, 500)
                            }
                        }
                    } else {
                        holder.btnDownload.isEnabled = true
                        holder.btnDownload.text = "İndir"
                        activeDownloads.remove(position)
                    }
                } catch (_: Exception) {
                    holder.btnDownload.isEnabled = true
                    holder.btnDownload.text = "İndir"
                    activeDownloads.remove(position)
                } finally {
                    cursor?.close()
                }
            }
        }

        handler.post(progressRunnable)
    }

    private fun mapJobType(jobType: String?): String {
        return when (jobType) {
            "jpg_to_pdf" -> "JPG to PDF"
            "pdf_to_word" -> "PDF to Word"
            "word_to_pdf" -> "Word to PDF"
            "pdf_merge" -> "PDF Birleştirme"
            else -> jobType ?: "-"
        }
    }

    private fun mapStatus(status: String?): String {
        return when (status) {
            "waiting" -> "Bekliyor"
            "processing" -> "İşleniyor"
            "done" -> "Tamamlandı"
            "failed" -> "Başarısız"
            else -> status ?: "-"
        }
    }
}