package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri

import android.icu.text.SimpleDateFormat
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.isletimsistemicalismasijetpackcompose.R
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotDefteriDetaySayfa(
    modifier: Modifier = Modifier,
    navController: NavController,
    notId: Int?,
    viewModel: NotDefteriViewModel
) {
    var baslik by remember { mutableStateOf("") }
    var notMetin by remember { mutableStateOf("") }
    val tarih = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date())
    }
    val context = LocalContext.current

    // Gelen notu yükle
    LaunchedEffect(notId) {
        notId?.let {
            val not = viewModel.getNotById(it)
            not?.let {
                baslik = not.baslik
                notMetin = not.notMetin
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Not Detayı")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (baslik.isBlank()) {
                        Toast.makeText(context, "Başlık Giriniz", Toast.LENGTH_SHORT).show()
                    } else if (notMetin.isBlank()) {
                        Toast.makeText(context, "Not Giriniz", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.yeniNot(
                            Not(
                                id = notId ?: 0,
                                baslik = baslik,
                                notMetin = notMetin,
                                listName = "null",
                                renk = "null",
                                tarih = tarih,
                                imageUrl = ""
                            )
                        )
                        navController.popBackStack()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Image(painter = painterResource(R.drawable.ic_add), contentDescription = "Kaydet")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = baslik,
                onValueChange = { baslik = it },
                label = { Text(text = "Başlık") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notMetin,
                onValueChange = { notMetin = it },
                label = { Text(text = "Not") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = tarih, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        notId?.let {
                            viewModel.notSil(it)
                            navController.popBackStack()
                        }
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil")
                }

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                }
            }
        }
    }
}
