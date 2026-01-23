package com.example.csvexplorer.adapter

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.csvexplorer.R
import com.example.csvexplorer.entity.RowEntity

import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONObject
import java.util.Locale

class RowAdapter(private val ctx: Context,private val onRowClick: ((RowEntity) -> Unit)? = null) : BaseAdapter() {

    private var items: List<RowEntity> = emptyList()
    private var headers: List<String> = emptyList()

    private var query: String = ""
    private var selectedColumn: String = "ALL_COLUMNS"

    fun submit(list: List<RowEntity>) {
        items = list
        notifyDataSetChanged()
    }

    fun setHeaders(h: List<String>) {
        headers = h
        notifyDataSetChanged()
    }

    fun setHighlight(q: String, selected: String) {
        query = q.trim()
        selectedColumn = selected
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): RowEntity = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = convertView ?: LayoutInflater.from(ctx).inflate(R.layout.row_item, parent, false)

        val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = v.findViewById<TextView>(R.id.tvSubtitle)
        val chipGroup = v.findViewById<ChipGroup>(R.id.chipGroup)

        val item = getItem(position)
        val obj = try { JSONObject(item.dataJson) } catch (_: Exception) { JSONObject() }

        val id = obj.optString("id", "")
        val first = obj.optString("first_name", obj.optString("firstname", ""))
        val last  = obj.optString("last_name", obj.optString("lastname", ""))
        val name = (first + " " + last).trim()

        val titleText = when {
            id.isNotBlank() && name.isNotBlank() -> "#$id  $name"
            id.isNotBlank() -> "#$id"
            name.isNotBlank() -> name
            else -> "Row ${position + 1}"
        }

        val lastSeen = obj.optString("last_seen", "")
        val country = obj.optString("country_title", "")
        val city = obj.optString("city_title", "")
        val subtitleText = when {
            lastSeen.isNotBlank() && (country.isNotBlank() || city.isNotBlank()) ->
                "Last seen: $lastSeen • ${listOf(country, city).filter { it.isNotBlank() }.joinToString(" / ")}"
            lastSeen.isNotBlank() -> "Last seen: $lastSeen"
            country.isNotBlank() || city.isNotBlank() ->
                listOf(country, city).filter { it.isNotBlank() }.joinToString(" / ")
            else -> buildFallbackSubtitle(obj)
        }

        tvTitle.text = highlightAll(titleText, query)
        tvSubtitle.text = highlightAll(subtitleText, query)

        chipGroup.removeAllViews()

        val chipPairs = buildChipPairs(obj)
        for ((k, valStr) in chipPairs) {
            val chip = Chip(ctx).apply {
                text = highlightAll("$k: $valStr", query)
                isClickable = false
                isCheckable = false
                isFocusable = false
                isEnabled = true
            }
            chipGroup.addView(chip)
        }
        v.setOnClickListener {
            onRowClick?.invoke(item)
        }
        return v
    }

    private fun buildFallbackSubtitle(obj: JSONObject): String {
        val keys = if (headers.isNotEmpty()) headers else obj.keys().asSequence().toList()
        val pairs = keys.map { it to obj.optString(it, "") }
            .filter { it.second.isNotBlank() }
            .take(2)

        return if (pairs.isEmpty()) "Tap to view details"
        else pairs.joinToString(" • ") { "${it.first}: ${it.second}" }
    }

    private fun buildChipPairs(obj: JSONObject): List<Pair<String, String>> {
        val keys = if (headers.isNotEmpty()) headers else obj.keys().asSequence().toList()

        val ordered = if (selectedColumn != "ALL_COLUMNS") {
            val rest = keys.filter { it != selectedColumn }
            listOf(selectedColumn) + rest
        } else keys

        val out = ArrayList<Pair<String, String>>()
        for (k in ordered) {
            val v = obj.optString(k, "")
            if (v.isNotBlank()) out.add(k to v)
            if (out.size >= 4) break
        }

        if (out.isEmpty()) {
            ordered.take(2).forEach { out.add(it to obj.optString(it, "-")) }
        }
        return out
    }

    private fun highlightAll(text: String, q: String): CharSequence {
        if (q.isBlank()) return text

        val lowerText = text.lowercase(Locale.getDefault())
        val lowerQ = q.lowercase(Locale.getDefault())
        var start = lowerText.indexOf(lowerQ)
        if (start < 0) return text

        val ss = SpannableString(text)
        while (start >= 0) {
            val end = (start + lowerQ.length).coerceAtMost(text.length)
            ss.setSpan(
                BackgroundColorSpan(0x33FFF59D), // soft yellow
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = lowerText.indexOf(lowerQ, end)
        }
        return ss
    }
}
