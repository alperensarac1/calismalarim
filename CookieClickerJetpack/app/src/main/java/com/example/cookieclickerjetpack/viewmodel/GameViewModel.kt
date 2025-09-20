package com.example.cookieclickerjetpack.viewmodel



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cookieclickerjetpack.entity.Prefs
import com.example.cookieclickerjetpack.model.FloatingText
import com.example.cookieclickerjetpack.model.Upgrade

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)

    private var _state = MutableStateFlow(prefs.loadGame())
    val state = _state.asStateFlow()

    private var _perks = MutableStateFlow(prefs.loadPerks())
    val perks = _perks.asStateFlow()

    private var _upgrades = MutableStateFlow(
        listOf(
            Upgrade(1, "Otomatik Tıklayıcı", "Saniyede +1", "Bolt", 50.0, cpsGain = 1.0),
            Upgrade(2, "Hızlı Karıştırıcı", "Tıklama +1", "FastForward", 75.0, tapGain = 1),
            Upgrade(3, "Minik Fırın", "Saniyede +5", "LocalFireDepartment", 250.0, cpsGain = 5.0),
            Upgrade(4, "Çikolata Parçaları", "Tıklama +3", "GridView", 300.0, tapGain = 3),
            Upgrade(5, "Pastane", "Saniyede +25", "Store", 1200.0, cpsGain = 25.0),
            Upgrade(6, "Fabrika", "Saniyede +120", "Factory", 6000.0, cpsGain = 120.0),
            Upgrade(7, "Araştırma Lab.", "Tıklama +10", "Science", 8000.0, tapGain = 10),
            Upgrade(8, "Roket Fırın", "Saniyede +750", "Rocket", 42000.0, cpsGain = 750.0)
        )
    )
    val upgrades = _upgrades.asStateFlow()

    private var _floaters = MutableStateFlow<List<FloatingText>>(emptyList())
    val floaters = _floaters.asStateFlow()

    // Crit
    private var _critReady = MutableStateFlow(true)
    val critReady = _critReady.asStateFlow()
    private var _critCooldownLeft = MutableStateFlow(0)
    val critCooldownLeft = _critCooldownLeft.asStateFlow()
    private var critJob: Job? = null

    private var loopJob: Job? = null

    init { startLoop() }

    // Derived
    private fun totalMultiplier(): Double {
        val prestige = 1.0 + _state.value.prestigeLevel * 0.10
        val gprod = 1.0 + _perks.value.gprod * 0.05
        return prestige * gprod
    }
    private fun discountPct(): Double = min(_perks.value.discount * 0.02, 0.50)
    private fun passiveCritChance(): Int = _perks.value.crit
    private fun tapPower(): Int = _state.value.baseTap + _state.value.extraTap + _perks.value.tapTop

    private fun persist() {
        prefs.saveGame(_state.value)
        prefs.savePerks(_perks.value)
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (true) {
                delay(100) // 0.1s
                val eff = _state.value.cps * totalMultiplier()
                _state.value = _state.value.copy(score = _state.value.score + eff * 0.1)
                persist()
            }
        }
    }

    fun onTapCookie(x: Float, y: Float) {
        var gain = (tapPower() * totalMultiplier()).toInt()
        if (passiveCritChance() > 0 && (0 until 100).random() < passiveCritChance()) {
            gain *= 3
            addFloating("CRIT +$gain", x, y, true)
        } else {
            addFloating("+$gain", x, y, false)
        }
        _state.value = _state.value.copy(score = _state.value.score + gain)
        persist()
    }

    fun doCrit(x: Float, y: Float) {
        if (!_critReady.value) return
        _critReady.value = false
        _critCooldownLeft.value = 30
        val gain = tapPower() * 10
        _state.value = _state.value.copy(score = _state.value.score + gain)
        addFloating("CRIT +$gain", x, y, true)
        critJob?.cancel()
        critJob = viewModelScope.launch {
            while (_critCooldownLeft.value > 0) {
                delay(1000)
                _critCooldownLeft.value = _critCooldownLeft.value - 1
            }
            _critReady.value = true
        }
        persist()
    }

    fun buyUpgrade(u: Upgrade) {
        val idx = _upgrades.value.indexOfFirst { it.id == u.id }
        if (idx == -1) return
        val price = u.currentPrice() * (1.0 - discountPct())
        if (_state.value.score < price) return

        val newLevel = _upgrades.value[idx].level + 1
        val newU = _upgrades.value[idx].copy(level = newLevel)
        val newList = _upgrades.value.toMutableList()
        newList[idx] = newU
        _upgrades.value = newList

        _state.value = _state.value.copy(
            score = _state.value.score - price,
            cps = _state.value.cps + u.cpsGain,
            extraTap = _state.value.extraTap + u.tapGain
        )
        persist()
    }

    fun reset() {
        _state.value = _state.value.copy(score = 0.0, cps = 0.0, extraTap = 0)
        _upgrades.value = _upgrades.value.map { it.copy(level = 0) }
        persist()
    }

    fun prestige() {
        val gain = sqrt(_state.value.score / 1000.0).toInt()
        if (gain <= 0) return
        _perks.value = _perks.value.copy(points = _perks.value.points + gain)
        _state.value = _state.value.copy(
            prestigeLevel = _state.value.prestigeLevel + gain,
            score = 0.0, cps = 0.0, extraTap = 0
        )
        _upgrades.value = _upgrades.value.map { it.copy(level = 0) }
        persist()
    }

    // Prestige Shop
    fun buyPerk(key: String, cost: Int, maxLevel: Int? = null) {
        if (_perks.value.points < cost) return
        _perks.value = _perks.value.copy(points = _perks.value.points - cost).let { cur ->
            when (key) {
                "gprod" -> cur.copy(gprod = cur.gprod + 1)
                "crit" -> cur.copy(crit = cur.crit + 1)
                "discount" -> {
                    val next = cur.discount + 1
                    cur.copy(discount = if (maxLevel != null) min(next, maxLevel) else next)
                }
                "tapTop" -> cur.copy(tapTop = cur.tapTop + 1)
                else -> cur
            }
        }
        persist()
    }

    private fun addFloating(text: String, x: Float, y: Float, isCrit: Boolean) {
        val id = System.nanoTime()
        _floaters.value = _floaters.value + FloatingText(id, text, x, y, isCrit)
        viewModelScope.launch {
            delay(700)
            _floaters.value = _floaters.value.filterNot { it.id == id }
        }
    }
}
