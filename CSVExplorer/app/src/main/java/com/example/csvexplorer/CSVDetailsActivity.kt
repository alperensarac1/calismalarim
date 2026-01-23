package com.example.csvexplorer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.csvexplorer.adapter.FieldAdapter
import com.example.csvexplorer.databinding.ActivityCsvdetailsBinding
import com.example.csvexplorer.model.FieldItem

import org.json.JSONObject
import java.util.Locale

class CSVDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCsvdetailsBinding

    private lateinit var adapter: FieldAdapter
    private var allFields: List<FieldItem> = emptyList()

    private var rowJson: String = ""
    private var headers: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCsvdetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rowJson = intent.getStringExtra(EXTRA_JSON).orEmpty()
        headers = intent.getStringArrayListExtra(EXTRA_HEADERS) ?: emptyList()

        val obj = try { JSONObject(rowJson) } catch (_: Exception) { JSONObject() }

        // Title
        val first = obj.optString("first_name", obj.optString("firstname", ""))
        val last  = obj.optString("last_name", obj.optString("lastname", ""))
        val id    = obj.optString("id", "")
        binding.tvTitle.text = when {
            id.isNotBlank() && (first.isNotBlank() || last.isNotBlank()) -> "#$id  $first $last"
            id.isNotBlank() -> "#$id"
            (first.isNotBlank() || last.isNotBlank()) -> "$first $last"
            else -> "CSV Row Details"
        }

        // RecyclerView
        adapter = FieldAdapter(this)
        binding.rvFields.layoutManager = LinearLayoutManager(this)
        binding.rvFields.adapter = adapter

        // build fields list
        allFields = buildFields(headers, obj)
        binding.tvCount.text = "${allFields.size} fields"
        adapter.submit(allFields, "")

        // search
        binding.etSearchDetails.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString()?.trim().orEmpty()
                val filtered = filterFields(allFields, q)
                binding.tvCount.text = "${filtered.size} matches"
                adapter.submit(filtered, q)
            }
        })

        // copy JSON
        binding.btnCopyJson.setOnClickListener {
            copyToClipboard("row_json", rowJson)
            Toast.makeText(this, "Copied JSON", Toast.LENGTH_SHORT).show()
        }

        // copy CSV row
        binding.btnCopyCsvRow.setOnClickListener {
            val csv = buildCsvRow(headers, obj)
            copyToClipboard("row_csv", csv)
            Toast.makeText(this, "Copied CSV row", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun filterFields(list: List<FieldItem>, q: String): List<FieldItem> {
        if (q.isBlank()) return list
        val qq = q.lowercase(Locale.getDefault())
        return list.filter {
            it.key.lowercase(Locale.getDefault()).contains(qq) ||
                    it.value.lowercase(Locale.getDefault()).contains(qq)
        }
    }

    private fun buildFields(headers: List<String>, obj: JSONObject): List<FieldItem> {
        val out = ArrayList<FieldItem>()

        if (headers.isNotEmpty()) {
            for (h in headers) {
                val v = obj.optString(h, "")
                out.add(FieldItem(h, if (v.isBlank()) "-" else v))
            }
            val extras = obj.keys().asSequence().toList().filter { it !in headers }.sorted()
            for (k in extras) {
                val v = obj.optString(k, "")
                out.add(FieldItem(k, if (v.isBlank()) "-" else v))
            }
        } else {
            val keys = obj.keys().asSequence().toList().sorted()
            for (k in keys) {
                val v = obj.optString(k, "")
                out.add(FieldItem(k, if (v.isBlank()) "-" else v))
            }
        }

        return out
    }

    private fun buildCsvRow(headers: List<String>, obj: JSONObject): String {
        if (headers.isEmpty()) return obj.toString()

        fun esc(value: String): String {
            val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
            var v = value.replace("\"", "\"\"")
            if (needsQuotes) v = "\"$v\""
            return v
        }

        val headerLine = headers.joinToString(",")
        val rowLine = headers.joinToString(",") { h -> esc(obj.optString(h, "")) }
        return headerLine + "\n" + rowLine
    }

    private fun copyToClipboard(label: String, text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    companion object {
        const val EXTRA_JSON = "extra_json"
        const val EXTRA_HEADERS = "extra_headers"
    }
}
