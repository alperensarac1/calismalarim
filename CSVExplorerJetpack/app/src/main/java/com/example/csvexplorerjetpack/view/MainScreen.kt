package com.example.csvexplorerjetpack.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.csvexplorerjetpack.data.RowEntity
import com.example.csvexplorerjetpack.viewmodel.MainViewModel
import org.json.JSONObject

@Composable
fun MainScreen(
    vm: MainViewModel,
    onPickCsv: () -> Unit,
    onOpenDetails: (String) -> Unit
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(14.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onPickCsv, enabled = !state.isLoading) { Text("Select CSV") }
            OutlinedButton(
                onClick = { vm.uploadCsv("https://alperensaracdeneme.com/deneme/upload_csv.php", context.contentResolver) },
                enabled = !state.isLoading && state.canUpload
            ) { Text("Get .xls") }
            OutlinedButton(onClick = { vm.applyFilter() }, enabled = !state.isLoading) { Text("Filter") }
            OutlinedButton(onClick = { vm.clearFilter() }, enabled = !state.isLoading) { Text("Clear") }
            TextButton(onClick = { vm.clearDb() }, enabled = !state.isLoading) { Text("Clear DB") }
        }

        Spacer(Modifier.height(10.dp))

        Text(state.infoText, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(10.dp))

        // Column chooser + query
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ColumnChooser(
                headers = state.headers,
                selected = state.selectedColumn,
                onSelected = vm::setSelectedColumn
            )
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::setQuery,
                label = { Text("Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(state.records) { item ->
                RowCard(item = item, headers = state.headers, onClick = { onOpenDetails(item.dataJson) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnChooser(headers: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val items = remember(headers) { listOf("ALL_COLUMNS") + headers }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Column") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { col ->
                DropdownMenuItem(
                    text = { Text(col) },
                    onClick = {
                        onSelected(col)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RowCard(item: RowEntity, headers: List<String>, onClick: () -> Unit) {
    val obj = remember(item.dataJson) { runCatching { JSONObject(item.dataJson) }.getOrDefault(JSONObject()) }

    val id = obj.optString("id", "")
    val first = obj.optString("first_name", obj.optString("firstname", ""))
    val last = obj.optString("last_name", obj.optString("lastname", ""))
    val title = when {
        id.isNotBlank() && (first.isNotBlank() || last.isNotBlank()) -> "#$id  ${(first + " " + last).trim()}"
        id.isNotBlank() -> "#$id"
        (first.isNotBlank() || last.isNotBlank()) -> (first + " " + last).trim()
        else -> "Row"
    }

    val subtitle = buildString {
        val lastSeen = obj.optString("last_seen", "")
        val country = obj.optString("country_title", "")
        val city = obj.optString("city_title", "")
        if (lastSeen.isNotBlank()) append("Last seen: $lastSeen")
        if (country.isNotBlank() || city.isNotBlank()) {
            if (isNotEmpty()) append(" • ")
            append(listOf(country, city).filter { it.isNotBlank() }.joinToString(" / "))
        }
        if (isEmpty()) append("Tap to view details")
    }

    Card(Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
