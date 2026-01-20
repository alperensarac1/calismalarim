package com.example.eticaretjetpack.view


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eticaretjetpack.model.ProductListDto
import com.example.eticaretjetpack.repo.HomeVMFactory
import com.example.eticaretjetpack.viewmodel.HomeViewModel


@Composable
fun HomeScreen(
    onGoCartTab: () -> Unit,
    onOpenProduct: (Int) -> Unit
) {
    val vm: HomeViewModel = viewModel(factory = HomeVMFactory())
    val st by vm.state.collectAsStateWithLifecycle()

    var search by remember { mutableStateOf("") }
    var sortExpanded by remember { mutableStateOf(false) }
    val sorts = listOf("newest", "price_asc", "price_desc")

    // first load
    LaunchedEffect(Unit) {
        vm.loadCategories()
        vm.loadProducts(page = 1)
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Anasayfa", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onGoCartTab) { Text("Sepet") }
        }

        // Search
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Ara") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val q = search.trim().ifEmpty { null }
                    vm.setFilters(st.filters.copy(q = q))
                }
            ) { Text("Ara") }

            FilterChip(
                selected = st.filters.discount,
                onClick = { vm.setFilters(st.filters.copy(discount = !st.filters.discount)) },
                label = { Text("İndirim") }
            )

            Box {
                OutlinedButton(onClick = { sortExpanded = true }) { Text("Sort: ${st.filters.sort}") }
                DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                    sorts.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                sortExpanded = false
                                vm.setFilters(st.filters.copy(sort = s))
                            }
                        )
                    }
                }
            }
        }

        // Categories
        Text("Kategoriler", fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                AssistChip(
                    onClick = { vm.setFilters(st.filters.copy(cat = null)) },
                    label = { Text("Tümü") }
                )
            }
            items(st.categories) { cat ->
                AssistChip(
                    onClick = { vm.setFilters(st.filters.copy(cat = cat.id)) },
                    label = { Text(cat.name) }
                )
            }
        }

        if (st.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        st.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        // Grid
        val gridState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(st.items, key = { it.id }) { item ->
                ProductCard(item = item, onClick = { onOpenProduct(item.id) })
            }
        }

        // paging trigger (VM append fixini aşağıda verdim)
        val shouldLoadNext by remember {
            derivedStateOf {
                val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = gridState.layoutInfo.totalItemsCount
                val nearEnd = last >= total - 4
                val hasMore = st.items.size < st.total
                nearEnd && hasMore && !st.loading
            }
        }
        LaunchedEffect(shouldLoadNext) {
            if (shouldLoadNext) vm.loadProducts(page = st.page + 1)
        }
    }
}

@Composable
private fun ProductCard(item: ProductListDto, onClick: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Text(item.name, maxLines = 2)
            Text("₺${"%.2f".format(item.price)}", fontWeight = FontWeight.Bold)
            item.discountPercent?.let {
                Text("İndirim: %${it}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
