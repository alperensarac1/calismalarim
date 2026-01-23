package com.example.csvexplorerjetpack

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.csvexplorerjetpack.navigation.AppNav
import com.example.csvexplorerjetpack.viewmodel.MainViewModel


class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pickCsv = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) vm.onCsvPicked(uri, contentResolver)
        }

        vm.init()

        setContent {
            MaterialTheme {
                Surface {
                    AppNav(
                        vm = vm,
                        onPickCsv = {
                            pickCsv.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel"))
                        }
                    )
                }
            }
        }
    }
}
