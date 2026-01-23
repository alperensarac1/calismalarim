package com.example.csvexplorerjetpack.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONObject

data class FieldItem(val key: String, val value: String)

@Composable
fun DetailsScreen(
    json: String,
    headers: List<String>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val obj = remember(json) { runCatching { JSONObject(json) }.getOrDefault(JSONObject()) }

    var q by remember { mutableStateOf("") }

    val allFields = remember(json, headers) { buildFields(headers, obj) }
    val filtered = remember(q, allFields) {
        if (q.isBlank()) allFields
        else allFields.filter { it.key.contains(q, true) || it.value.contains(q, true) }
    }

    Column(Modifier.fillMaxSize().padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Details", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            label = { Text("Search in fields") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { copy(context, "row_json", json) }) { Text("Copy JSON") }
            OutlinedButton(onClick = { copy(context, "row_csv", buildCsv(headers, obj)) }) { Text("Copy CSV") }
        }

        Spacer(Modifier.height(10.dp))
        Text("${filtered.size} fields", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.key, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(item.value, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun buildFields(headers: List<String>, obj: JSONObject): List<FieldItem> {
    val out = mutableListOf<FieldItem>()

    if (headers.isNotEmpty()) {
        headers.forEach { h ->
            val v = obj.optString(h, "")
            out.add(FieldItem(h, if (v.isBlank()) "-" else v))
        }
        // extras
        val extras = mutableListOf<String>()
        val it = obj.keys()
        while (it.hasNext()) {
            val k = it.next()
            if (!headers.contains(k)) extras.add(k)
        }
        extras.sorted().forEach { k ->
            val v = obj.optString(k, "")
            out.add(FieldItem(k, if (v.isBlank()) "-" else v))
        }
    } else {
        val keys = mutableListOf<String>()
        val it2 = obj.keys()
        while (it2.hasNext()) keys.add(it2.next())
        keys.sorted().forEach { k ->
            val v = obj.optString(k, "")
            out.add(FieldItem(k, if (v.isBlank()) "-" else v))
        }
    }

    return out
}

private fun buildCsv(headers: List<String>, obj: JSONObject): String {
    if (headers.isEmpty()) return obj.toString()
    val headerLine = headers.joinToString(",")
    val rowLine = headers.joinToString(",") { esc(obj.optString(it, "")) }
    return headerLine + "\n" + rowLine
}

private fun esc(value0: String): String {
    var value = value0
    val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
    value = value.replace("\"", "\"\"")
    if (needsQuotes) value = "\"$value\""
    return value
}

private fun copy(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}
