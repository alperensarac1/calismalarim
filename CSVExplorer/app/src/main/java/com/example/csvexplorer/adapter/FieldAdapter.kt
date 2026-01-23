package com.example.csvexplorer.adapter


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.csvexplorer.R
import com.example.csvexplorer.model.FieldItem
import java.util.Locale

class FieldAdapter(
    private val context: Context
) : RecyclerView.Adapter<FieldAdapter.VH>() {

    private var items: List<FieldItem> = emptyList()
    private var query: String = ""

    fun submit(list: List<FieldItem>, searchQuery: String) {
        items = list
        query = searchQuery.trim()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_field, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvKey.text = highlight(item.key, query)
        holder.tvValue.text = highlight(item.value, query)

        holder.btnCopy.setOnClickListener {
            copyToClipboard("field_value", item.value)
            Toast.makeText(context, "Copied: ${item.key}", Toast.LENGTH_SHORT).show()
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvKey: TextView = itemView.findViewById(R.id.tvKey)
        val tvValue: TextView = itemView.findViewById(R.id.tvValue)
        val btnCopy: TextView = itemView.findViewById(R.id.btnCopy)
    }

    private fun copyToClipboard(label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun highlight(text: String, q: String): CharSequence {
        if (q.isBlank()) return text
        val lowerText = text.lowercase(Locale.getDefault())
        val lowerQ = q.lowercase(Locale.getDefault())

        val start = lowerText.indexOf(lowerQ)
        if (start < 0) return text

        val end = start + q.length
        val ss = SpannableString(text)
        ss.setSpan(BackgroundColorSpan(0x33FFF59D), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss
    }
}
