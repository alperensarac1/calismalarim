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
import coil.compose.AsyncImage
import com.example.eticaretjetpack.model.CartItemDto
import com.example.eticaretjetpack.model.CheckoutRequest
import com.example.eticaretjetpack.repo.CartVMFactory
import com.example.eticaretjetpack.repo.OrdersVMFactory
import com.example.eticaretjetpack.viewmodel.CartViewModel
import com.example.eticaretjetpack.viewmodel.OrdersViewModel


@Composable
fun CartScreen(
    onOrderCreatedGoDetail: (Int) -> Unit
) {
    val ctx = LocalContext.current
    val cartVm: CartViewModel = viewModel(factory = CartVMFactory(ctx))
    val ordersVm: OrdersViewModel = viewModel(factory = OrdersVMFactory(ctx))

    val cartSt by cartVm.state.collectAsStateWithLifecycle()
    val orderSt by ordersVm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { cartVm.loadCart() }

    LaunchedEffect(orderSt.lastOrder) {
        val created = orderSt.lastOrder
        if (created != null) {
            // order id alanı sende "orderId" diye dönüyor
            onOrderCreatedGoDetail(created.orderId)
        }
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Sepet", style = MaterialTheme.typography.headlineSmall)

        if (cartSt.loading || orderSt.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        cartSt.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        orderSt.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        val items = cartSt.cart?.items.orEmpty()

        if (!cartSt.loading && items.isEmpty()) {
            Text("Sepet boş")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(items, key = { it.item_id }) { item ->
                    CartRow(
                        item = item,
                        busy = cartSt.busyItemId == item.item_id,
                        onPlus = { cartVm.inc(item.item_id, item.quantity) },
                        onMinus = { cartVm.dec(item.item_id, item.quantity) },
                        onDelete = { cartVm.delete(item.item_id) }
                    )
                }
            }
        }

        val totalItems = cartSt.cart?.total_items ?: 0
        val total = cartSt.cart?.total ?: 0.0

        Text("Ürün: $totalItems")
        Text("Toplam: ₺${"%.2f".format(total)}")

        Button(
            onClick = {
                val req = CheckoutRequest(
                    addressName = "Ev",
                    addressLine1 = "Test Mah. Test Sok. No:1",
                    city = "Istanbul",
                    district = "Kadikoy",
                    postalCode = "34000"
                )
                ordersVm.checkout(req)
            },
            enabled = items.isNotEmpty() && !orderSt.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Checkout")
        }
    }
}

@Composable
private fun CartRow(
    item: CartItemDto,
    busy: Boolean,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AsyncImage(
                model = item.image_url,
                contentDescription = item.name,
                modifier = Modifier.size(70.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text("₺${"%.2f".format(item.sale_price)}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onMinus, enabled = !busy) { Text("-") }
                    Text("${item.quantity}", modifier = Modifier.padding(top = 10.dp))
                    OutlinedButton(onClick = onPlus, enabled = !busy) { Text("+") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDelete, enabled = !busy) { Text("Sil") }
                }
            }
        }
    }
}
