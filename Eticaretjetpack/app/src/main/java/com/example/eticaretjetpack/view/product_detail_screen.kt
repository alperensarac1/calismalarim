package com.example.eticaretjetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eticaretjetpack.repo.ProductDetailVMFactory
import com.example.eticaretjetpack.viewmodel.ProductDetailViewModel


@Composable
fun ProductDetailScreen(productId: Int) {
    val ctx = LocalContext.current
    val vm: ProductDetailViewModel = viewModel(factory = ProductDetailVMFactory(ctx))
    val st by vm.state.collectAsStateWithLifecycle()

    var qty by remember { mutableIntStateOf(1) }

    LaunchedEffect(productId) {
        if (productId != -1) vm.load(productId)
    }

    LaunchedEffect(st.addSuccess) {
        if (st.addSuccess) vm.clearFlags()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ürün Detay", style = MaterialTheme.typography.headlineSmall)

        if (st.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

        st.product?.let { p ->
            AsyncImage(
                model = p.imageUrl,
                contentDescription = p.name,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
            Text(p.name, style = MaterialTheme.typography.titleLarge)
            Text("₺${"%.2f".format(p.price)}", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { qty = (qty - 1).coerceAtLeast(1) }) { Text("-") }
                Text("$qty", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { qty = (qty + 1).coerceAtMost(99) }) { Text("+") }
            }

            Button(
                onClick = { vm.addToCart(productId, qty) },
                enabled = !st.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sepete Ekle")
            }
        }

        st.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
