package com.example.cookieclickerjetpack



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cookieclickerjetpack.model.FloatingText
import com.example.cookieclickerjetpack.model.Upgrade
import com.example.cookieclickerjetpack.viewmodel.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "main") {
        composable("main") { MainScreen(onOpenShop = { nav.navigate("shop") }) }
        composable("shop") { PrestigeShopScreen(onBack = { nav.popBackStack() }) }
    }
}

@Composable
fun MainScreen(onOpenShop: () -> Unit, vm: GameViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val perks by vm.perks.collectAsState()
    val upgrades by vm.upgrades.collectAsState()
    val floaters by vm.floaters.collectAsState()
    val critReady by vm.critReady.collectAsState()
    val critLeft by vm.critCooldownLeft.collectAsState()

    var cookieScale by remember { mutableStateOf(1f) }
    val scaleAnim by animateFloatAsState(targetValue = cookieScale)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF3E5), Color(0xFFFFE0B2))
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {

            // Üst kart
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(format(state.score), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${format(state.cps /* eff cps UI'da istersen çarpanı hesaplayıp gösterebilirsin */)} / sn",
                        color = Color.DarkGray)
                }
            }

            // Cookie alanı
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {

                val interaction = remember { MutableInteractionSource() }

                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = "cookie",
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer { scaleX = scaleAnim; scaleY = scaleAnim }
                        .clickable(
                            interactionSource = interaction,
                            indication = LocalIndication.current
                        ) {
                            cookieScale = 0.92f
                            vm.onTapCookie(density.density,density.density)
                        },
                    tint = Color(0xFFCE9B62)
                )

                Text("🍪", fontSize = MaterialTheme.typography.displayMedium.fontSize)
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                item {
                    Text("Yükseltmeler", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 6.dp))
                }
                items(upgrades, key = { it.id }) { u ->
                    UpgradeRow(
                        upgrade = u,
                        price = u.currentPrice() * (1.0 - vmDiscountPct(vm)),
                        canAfford = state.score >= u.currentPrice() * (1.0 - vmDiscountPct(vm)),
                        onBuy = { vm.buyUpgrade(u) }
                    )
                }
            }

            // Bottom bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0x22FFFFFF))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenShop,
                    modifier = Modifier.weight(1f)
                ) { Text("Shop") }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { vm.prestige() },
                    modifier = Modifier.weight(1f)
                ) { Text("Prestige") }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { vm.reset() }
                ) { Text("Reset") }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        vm.doCrit(x = 110f, y = 110f)
                    },
                    enabled = critReady
                ) {
                    Text(if (critReady) "Crit" else "${critLeft}s")
                }
            }
        }

        // Floating texts overlay (basit çizim)
        floaters.forEach { f ->
            FloatingTextBubble(f)
        }
    }
}

private fun vmDiscountPct(vm: GameViewModel): Double {
    val discount = vm.perks.value.discount
    return min(discount * 0.02, 0.50)
}

@Composable
fun FloatingTextBubble(f: FloatingText) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = f.x.dp, top = max(0f, f.y - 120).dp)
    ) {
        Text(
            f.text,
            color = if (f.isCrit) Color.Red else Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color(0x66000000))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun UpgradeRow(upgrade: Upgrade, price: Double, canAfford: Boolean, onBuy: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (upgrade.icon) {
                "Bolt" -> Icons.Default.Bolt
                "FastForward" -> Icons.Default.FastForward
                "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
                "GridView" -> Icons.Default.GridView
                "Store" -> Icons.Default.Store
                "Factory" -> Icons.Default.Factory
                "Science" -> Icons.Default.Science
                "Rocket" -> Icons.Default.Rocket
                else -> Icons.Default.Star
            }
            Icon(icon, contentDescription = null, tint = Color(0xFF795548), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (upgrade.level > 0) "${upgrade.title} (Lv ${upgrade.level})" else upgrade.title,
                    fontWeight = FontWeight.SemiBold)
                Text(upgrade.desc, color = Color.DarkGray)
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(format(price))
                OutlinedButton(onClick = onBuy, enabled = canAfford) { Text("Buy") }
            }
        }
    }
}

private fun format(v: Double): String =
    when {
        v >= 1_000_000 -> String.format("%.2fM", v / 1_000_000)
        v >= 1_000 -> String.format("%.1fk", v / 1_000)
        else -> String.format("%.0f", v)
    }
