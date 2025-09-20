package com.example.cookieclickerjetpack


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cookieclickerjetpack.viewmodel.GameViewModel
import kotlin.math.pow

data class PerkUi(
    val key: String,
    val title: String,
    val desc: String,
    val baseCost: Int,
    val scaling: Double,
    val level: Int,
    val maxLevel: Int = Int.MAX_VALUE
) {
    fun costForNext(): Int = (baseCost * scaling.pow(level)).toInt().coerceAtLeast(baseCost)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestigeShopScreen(onBack: () -> Unit, vm: GameViewModel = viewModel()) {
    val perks by vm.perks.collectAsState()

    val items = listOf(
        PerkUi("gprod","Altın Çırpıcı","%5 üretim çarpanı / seviye (CPS & tap)",1,1.6, perks.gprod),
        PerkUi("crit","Uğurlu Tılsım","%1 pasif crit şansı / seviye (tap x3)",2,1.7, perks.crit),
        PerkUi("discount","Toplu Alım","Upgrade fiyatlarında %2 indirim / seviye (maks %50)",3,1.8, perks.discount, maxLevel = 25),
        PerkUi("tapTop","Turbo Tap","Kalıcı +1 tap gücü / seviye",2,1.5, perks.tapTop)
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Prestige Mağazası") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Geri") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text("Prestige Puanı: ${perks.points}", modifier = Modifier.padding(16.dp))
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(items, key = { it.key }) { p ->
                    val cost = p.costForNext()
                    val can = perks.points >= cost && p.level < p.maxLevel
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${p.title} (Lv ${p.level})", style = MaterialTheme.typography.titleMedium)
                            Text(p.desc, color = androidx.compose.ui.graphics.Color.DarkGray)
                            Spacer(Modifier.height(6.dp))
                            Row {
                                Text("Maliyet: $cost", modifier = Modifier.weight(1f))
                                Button(onClick = { vm.buyPerk(p.key, cost, p.maxLevel) }, enabled = can) {
                                    Text("Satın Al")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
