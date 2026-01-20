package com.example.eticaretjetpack.view



import androidx.compose.foundation.clickable
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
fun OrdersScreen(
    onOpenOrder: (Int) -> Unit
) {
    val ctx = LocalContext.current
    val vm: OrdersViewModel = viewModel(factory = OrdersVMFactory(ctx))
    val st by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadOrders() }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Siparişler", style = MaterialTheme.typography.headlineSmall)

        if (st.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        st.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (!st.loading && st.orders.isEmpty()) {
            Text("Sipariş yok")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(st.orders, key = { it.id }) { o ->
                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenOrder(o.id) }
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Sipariş #${o.id}", style = MaterialTheme.typography.titleMedium)
                            Text("Durum: ${o.status}")
                            Text("Toplam: ${o.currency} ${"%.2f".format(o.totalAmount)}")
                            Text("Tarih: ${o.createdAt}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
