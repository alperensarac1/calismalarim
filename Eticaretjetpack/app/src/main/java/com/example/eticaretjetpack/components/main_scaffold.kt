package com.example.eticaretjetpack.components



import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eticaretjetpack.navigation.Routes
import com.example.eticaretjetpack.view.CartScreen
import com.example.eticaretjetpack.view.HomeScreen
import com.example.eticaretjetpack.view.OrderDetailScreen
import com.example.eticaretjetpack.view.OrdersScreen
import com.example.eticaretjetpack.view.ProductDetailScreen
import com.example.eticaretjetpack.view.SettingsScreen


@Composable
fun MainScaffold(
    rootNav: NavHostController,
    startTab: String
) {
    val nav = rememberNavController()

    LaunchedEffect(startTab) {
        nav.navigate(startTab) {
            popUpTo(nav.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = nav.currentDestination?.route,
                onTab = { route ->
                    nav.navigate(route) {
                        launchSingleTop = true
                        popUpTo(nav.graph.startDestinationId) { saveState = true }
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onGoCartTab = { nav.navigate(Routes.CART) },
                    onOpenProduct = { id -> nav.navigate(Routes.productDetail(id)) }
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    onOrderCreatedGoDetail = { orderId ->
                        nav.navigate(Routes.orderDetail(orderId))
                    }
                )
            }
            composable(Routes.ORDERS) {
                OrdersScreen(
                    onOpenOrder = { id -> nav.navigate(Routes.orderDetail(id)) }
                )
            }
            composable(Routes.SETTINGS) { SettingsScreen() }

            composable("${Routes.PRODUCT_DETAIL}/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toIntOrNull() ?: -1
                ProductDetailScreen(productId = id)
            }

            composable("${Routes.ORDER_DETAIL}/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toIntOrNull() ?: -1
                OrderDetailScreen(orderId = id)
            }
        }
    }
}

@Composable
private fun BottomBar(
    currentRoute: String?,
    onTab: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = { onTab(Routes.HOME) },
            label = { Text("Anasayfa") },
            icon = { }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.CART,
            onClick = { onTab(Routes.CART) },
            label = { Text("Sepet") },
            icon = { }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.ORDERS,
            onClick = { onTab(Routes.ORDERS) },
            label = { Text("Siparişler") },
            icon = { }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.SETTINGS,
            onClick = { onTab(Routes.SETTINGS) },
            label = { Text("Ayarlar") },
            icon = { }
        )
    }
}
