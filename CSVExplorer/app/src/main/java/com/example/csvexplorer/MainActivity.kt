package com.example.csvexplorer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.csvexplorer.adapter.RowAdapter
import com.example.csvexplorer.databinding.ActivityMainBinding
import com.example.csvexplorer.entity.HeadersStore
import com.example.csvexplorer.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    private lateinit var spinnerAdapter: ArrayAdapter<String>

    private val uploadEndpoint = "https://alperensaracdeneme.com/deneme/upload_csv.php"

    private lateinit var rowAdapter: RowAdapter

    private val pickCsv = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) vm.onCsvPicked(uri, contentResolver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mutableListOf())
        binding.spinnerColumn.adapter = spinnerAdapter

        rowAdapter = RowAdapter(this) { item ->
            val intent = Intent(this, CSVDetailsActivity::class.java).apply {
                putExtra(CSVDetailsActivity.EXTRA_JSON, item.dataJson)
                putStringArrayListExtra(
                    CSVDetailsActivity.EXTRA_HEADERS,
                    ArrayList(HeadersStore.load(this@MainActivity))
                )
            }
            startActivity(intent)
        }
        binding.listView.adapter = rowAdapter

        binding.listView.adapter = rowAdapter

        binding.btnPickCsv.setOnClickListener {
            pickCsv.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel"))
        }



        binding.btnSearch.setOnClickListener {
            val q = binding.etQuery.text?.toString().orEmpty()
            val col = binding.spinnerColumn.selectedItem?.toString() ?: "ALL_COLUMNS"
            vm.setQuery(q)
            vm.setSelectedColumn(col)
            vm.applyFilter()
        }

        binding.btnClear.setOnClickListener {
            binding.etQuery.setText("")
            if (spinnerAdapter.count > 0) binding.spinnerColumn.setSelection(0)
            vm.clearFilter()

            rowAdapter.setHighlight("", "ALL_COLUMNS")
        }

        binding.btnUpload.setOnClickListener {
            vm.upload(uploadEndpoint, contentResolver)
        }

        try {
            binding.btnClearDb.setOnClickListener {
                lifecycleScope.launch {
                    vm.clearDb()
                }
            }
        } catch (_: Exception) {
        }


        lifecycleScope.launch {
            vm.state.collect { s ->

                binding.btnUpload.isEnabled = s.canUpload && !s.isLoading
                binding.btnUpload.alpha = if (binding.btnUpload.isEnabled) 1f else 0.5f
                binding.btnPickCsv.isEnabled = !s.isLoading
                binding.btnSearch.isEnabled = !s.isLoading
                binding.btnClear.isEnabled = !s.isLoading

                binding.tvInfo.text = s.infoText

                val prevSelected = binding.spinnerColumn.selectedItem?.toString()
                val items = mutableListOf<String>().apply {
                    add("ALL_COLUMNS")
                    addAll(s.headers)
                }

                spinnerAdapter.clear()
                spinnerAdapter.addAll(items)
                spinnerAdapter.notifyDataSetChanged()

                if (!prevSelected.isNullOrBlank()) {
                    val idx = items.indexOf(prevSelected)
                    if (idx >= 0) binding.spinnerColumn.setSelection(idx)
                }

                rowAdapter.setHeaders(s.headers)
                rowAdapter.submit(s.records)

                rowAdapter.setHighlight(s.query, s.selectedColumn)

                s.errorMessage?.let { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                }

                s.downloadUrl?.let { url ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Cannot open browser: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        vm.consumeDownloadUrl()
                    }
                }
            }
        }

        vm.init()
    }
}
