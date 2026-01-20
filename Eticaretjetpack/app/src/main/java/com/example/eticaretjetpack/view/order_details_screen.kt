package com.example.eticaretjetpack.view


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eticaretjetpack.repo.OrdersVMFactory
import com.example.eticaretjetpack.viewmodel.OrdersViewModel


@Composable
fun OrderDetailScreen(orderId: Int) {
    val ctx = LocalContext.current
    val vm: OrdersViewModel = viewModel(factory = OrdersVMFactory(ctx))
    val st by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        if (orderId != -1) vm.loadOrderDetail(orderId)
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Sipariş Detay #$orderId", style = MaterialTheme.typography.headlineSmall)

        if (st.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        st.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        val d = st.orderDetail
        if (d == null && !st.loading) {
            Text("Detay bulunamadı")
            return@Column
        }

        d?.let { detail ->
            Text("Durum: ${detail.status}")
            Text("Toplam: ${detail.currency} ${"%.2f".format(detail.totalAmount)}")
            Text("Adres: ${detail.addressName ?: ""} - ${detail.addressLine1 ?: ""}")

            Divider()

            Text("Ürünler", style = MaterialTheme.typography.titleMedium)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(detail.items) { it ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(it.name, style = MaterialTheme.typography.titleSmall)
                            Text("Adet: ${it.quantity}")
                            Text("Birim: ₺${"%.2f".format(it.unitPrice)}")
                            Text("Satır: ₺${"%.2f".format(it.lineTotal)}")
                        }
                    }
                }
            }
        }
    }
}
