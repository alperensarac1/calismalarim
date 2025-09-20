package com.example.cookieclickerkotlin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookieclickerkotlin.databinding.ActivityMainBinding
import java.text.DecimalFormat
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), UpgradeAdapter.Listener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UpgradeAdapter

    private var prestigePoints = 0

    // Perk seviyeleri
    private var perkGProd = 0
    private var perkCrit = 0
    private var perkDiscount = 0
    private var perkTapTop = 0
    private val passiveCritMultiplier = 3

    // --- Oyun durumu ---
    private var score: Double = 0.0
    private var cps: Double = 0.0
    private var baseTap: Int = 1
    private var extraTapFromUpgrades: Int = 0
    private val tapPower: Int get() = baseTap + extraTapFromUpgrades

    // --- Prestige ---
    private val prefs by lazy { getSharedPreferences("cookie_prefs", MODE_PRIVATE) }
    private var prestigeLevel = 0           // toplam prestij seviyesi
    private var prestigeMultiplier = 1.0    // üretime uygulanır (tap ve cps)
    private fun totalMultiplier(): Double {
        val prestige = 1.0 + (prestigeLevel * 0.10)
        val gprod   = 1.0 + (perkGProd * 0.05)
        return prestige * gprod
    }
    private fun effectiveCps(): Double = cps * totalMultiplier()
    private fun effectiveTap(): Int = (tapPower * totalMultiplier()).toInt().coerceAtLeast(1)


    // Ayarlar
    private var vibrateEnabled = true
    private var soundEnabled = true
    private var autosaveSec = 10

    // Ses
    private var soundPool: SoundPool? = null
    private var soundClickId: Int = 0
    private var soundLoaded = false

    // Döngüler
    private val handler = Handler(Looper.getMainLooper())
    private val tickMs = 100L
    private val ticker = object : Runnable {
        override fun run() {
            val eff = effectiveCps()
            if (eff > 0.0) {
                score += eff / (1000.0 / tickMs)
                updateUi()
            }
            handler.postDelayed(this, tickMs)
        }
    }
    private val autosaver = object : Runnable {
        override fun run() {
            saveState()
            handler.postDelayed(this, (autosaveSec * 1000L).coerceAtLeast(5000L))
        }
    }

    // Crit
    private val critMultiplier = 10
    private val critCooldownSec = 30
    private var critReady = true
    private var critCooldownLeft = 0
    private val critTicker = object : Runnable {
        override fun run() {
            if (critCooldownLeft > 0) {
                critCooldownLeft--
                updateCritUi()
                handler.postDelayed(this, 1000L)
            } else {
                critReady = true
                updateCritUi()
            }
        }
    }

    // Yükseltmeler
    private val upgrades = mutableListOf(
        Upgrade(1, "Otomatik Tıklayıcı", "...", R.drawable.ic_upgrade_autoclicker, 50.0, cpsGain = 1.0),
        Upgrade(2, "Hızlı Karıştırıcı",  "...", R.drawable.ic_upgrade_mixer,       75.0, tapGain = 1),
        Upgrade(3, "Minik Fırın",         "...", R.drawable.ic_upgrade_oven,       250.0, cpsGain = 5.0),
        Upgrade(4, "Çikolata Parçaları",  "...", R.drawable.ic_upgrade_choco,      300.0, tapGain = 3),
        Upgrade(5, "Pastane",              "...", R.drawable.ic_upgrade_bakery,     1200.0, cpsGain = 25.0),
        Upgrade(6, "Fabrika",              "...", R.drawable.ic_upgrade_factory,    6000.0, cpsGain = 120.0),
        Upgrade(7, "Araştırma Lab.",       "...", R.drawable.ic_upgrade_lab,        8000.0, tapGain = 10),
        Upgrade(8, "Roket Fırın",          "...", R.drawable.ic_upgrade_rocket,     42000.0, cpsGain = 750.0)

    )

    private val dfScore = DecimalFormat("#,###.##")
    private val dfCps = DecimalFormat("#,###.##")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        // Kısayol: Prestige'e uzun basınca Settings
        binding.btnPrestige.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        loadState()
        refreshSettings()
        loadPrestige()
        initSound()

        setupRecycler()
        setupCookieClick()
        setupButtons()
        updateUi()

        binding.flyingTextContainer.doOnLayout { /* ready */ }
    }

    override fun onStart() {
        super.onStart()
        handler.post(ticker)
        handler.postDelayed(autosaver, (autosaveSec * 1000L).coerceAtLeast(5000L))
    }

    override fun onResume() {
        super.onResume()
        refreshSettings()
        loadPrestige()
        updateUi()
        updateCritUi()
    }


    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(ticker)
        handler.removeCallbacks(autosaver)
        handler.removeCallbacks(critTicker)
        saveState()
        savePrestige()
        releaseSound()
    }

    // --- UI ---
    private fun setupRecycler() {
        adapter = UpgradeAdapter(upgrades, this)
        binding.rvUpgrades.layoutManager = LinearLayoutManager(this)
        binding.rvUpgrades.adapter = adapter
        adapter.updateAffordability(score)
    }

    private fun setupCookieClick() {
        val clickAnim = AnimationUtils.loadAnimation(this, R.anim.click_bounce)

        var lastTouchX = 0f
        var lastTouchY = 0f
        binding.btnCookie.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                lastTouchX = event.rawX
                lastTouchY = event.rawY
            }
            false
        }

        binding.btnCookie.setOnClickListener { v ->
            var gain = effectiveTap()
            // pasif crit: yüzde (perkCrit) ihtimalle x3
            if (perkCrit > 0) {
                val chance = perkCrit.coerceAtMost(100) // güvenlik
                if ((0..99).random() < chance) {
                    gain *= passiveCritMultiplier
                    spawnFlyingText("CRIT +$gain", lastTouchX, lastTouchY)
                } else {
                    spawnFlyingText("+$gain", lastTouchX, lastTouchY)
                }
            } else {
                spawnFlyingText("+$gain", lastTouchX, lastTouchY)
            }
            score += gain
            v.startAnimation(clickAnim)
            haptic()
            playClickSound()
            updateUi()
        }
    }

    private fun setupButtons() {
        binding.btnPrestige.setOnClickListener { confirmPrestige() }
        binding.btnCrit.setOnClickListener { tryCritClick() }
        binding.btnReset.setOnClickListener { confirmReset() }
        binding.btnShop.setOnClickListener {
            startActivity(Intent(this, PrestigeShopActivity::class.java))
        }

        updateCritUi()
    }

    private fun updateCritUi() {
        binding.btnCrit.isEnabled = critReady
        binding.btnCrit.text = if (critReady) getString(R.string.crit) else "${critCooldownLeft}s"
    }

    private fun updateUi() {
        binding.tvScore.text = dfScore.format(score)
        // İsteğe bağlı multiplier bilgisi:
        val mult = String.format("x%.2f", totalMultiplier())
        binding.tvCps.text = "${dfCps.format(effectiveCps())} / sn  ($mult)"
        val discountPct = (perkDiscount * 0.02).coerceAtMost(0.50)
        adapter.updateAffordability(score, discountPct)
    }


    // --- UpgradeAdapter.Listener ---
    override fun onBuyClicked(item: Upgrade) {
        val discountPct = (perkDiscount * 0.02).coerceAtMost(0.50) // %50 tavan
        val price = item.currentPrice() * (1.0 - discountPct)
        if (score >= price) {
            score -= price
            item.level += 1
            cps += item.cpsGain
            extraTapFromUpgrades += item.tapGain
            adapter.notifyItemChanged(upgrades.indexOf(item))
            updateUi()
        }
    }


    // --- Prestige ---
    private fun loadPrestige() {
        prestigeLevel = prefs.getInt("prestige_level", 0)
        prestigeMultiplier = prefs.getFloat("prestige_mult", 1f).toDouble().coerceAtLeast(1.0)
        prestigePoints = prefs.getInt("prestige_points", 0)

        perkGProd    = prefs.getInt("perk_gprod", 0)
        perkCrit     = prefs.getInt("perk_crit", 0)
        perkDiscount = prefs.getInt("perk_discount", 0)
        perkTapTop   = prefs.getInt("perk_taptop", 0)

        // Perk etkileri uygula
        baseTap = 1 + perkTapTop
    }

    private fun savePrestige() {
        prefs.edit()
            .putInt("prestige_level", prestigeLevel)
            .putFloat("prestige_mult", prestigeMultiplier.toFloat())
            .putInt("prestige_points", prestigePoints)
            .putInt("perk_gprod", perkGProd)
            .putInt("perk_crit", perkCrit)
            .putInt("perk_discount", perkDiscount)
            .putInt("perk_taptop", perkTapTop)
            .apply()
    }



    // Basit bir kazanç formülü: sqrt(score / 1000). Her prestij seviyesi %10 üretim çarpanı verir.
    private fun calcPrestigeGain(currentScore: Double): Int {
        val raw = sqrt(currentScore / 1000.0)
        return raw.toInt().coerceAtLeast(0)
    }

    private fun confirmPrestige() {
        val gain = calcPrestigeGain(score)
        if (gain <= 0) {
            // Yeterli skor yoksa küçük bir uyarı animasyonu
            binding.btnPrestige.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pop_in))
            return
        }
        val percent = gain * 10
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.prestige_confirm_title))
            .setMessage(getString(R.string.prestige_confirm_msg, percent))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> doPrestige(gain) }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun doPrestige(gain: Int) {
        prestigeLevel += gain
        prestigeMultiplier = 1.0 + (prestigeLevel * 0.10)

        // yeni: prestij puanı ver
        prestigePoints += gain
        savePrestige()

        // oyun reset (çarpan & perkler kalır)
        score = 0.0; cps = 0.0; extraTapFromUpgrades = 0
        upgrades.forEach { it.level = 0 }
        saveState()
        updateUi()
        binding.btnPrestige.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pop_in))
    }


    // --- Crit ---
    private fun tryCritClick() {
        if (!critReady) return
        critReady = false
        val gain = effectiveTap() * critMultiplier
        score += gain
        spawnFlyingText("+$gain", binding.btnCookie.x + binding.btnCookie.width / 2f, binding.btnCookie.y)
        haptic()
        playClickSound()
        updateUi()

        critCooldownLeft = critCooldownSec
        updateCritUi()
        handler.post(critTicker)
    }

    // --- Reset ---
    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reset_confirm_title))
            .setMessage(getString(R.string.reset_confirm_msg))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> doFullReset() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun doFullReset() {
        // Prestige ÇARPANI KALIR
        score = 0.0
        cps = 0.0
        extraTapFromUpgrades = 0
        upgrades.forEach { it.level = 0 }
        // crit durumu
        critReady = true
        critCooldownLeft = 0
        handler.removeCallbacks(critTicker)

        saveState()
        updateUi()
        updateCritUi()
    }

    // --- Kalıcılık (oyun state) ---
    private fun saveState() {
        val sp = getSharedPreferences("cookie_state", MODE_PRIVATE).edit()
        sp.putFloat("score", score.toFloat())
        sp.putFloat("cps", cps.toFloat())
        sp.putInt("extraTap", extraTapFromUpgrades)
        upgrades.forEachIndexed { index, up -> sp.putInt("level_$index", up.level) }
        sp.apply()
    }

    private fun loadState() {
        val sp = getSharedPreferences("cookie_state", MODE_PRIVATE)
        score = sp.getFloat("score", 0f).toDouble()
        cps = sp.getFloat("cps", 0f).toDouble()
        extraTapFromUpgrades = sp.getInt("extraTap", 0)
        upgrades.forEachIndexed { index, up ->
            val savedLevel = sp.getInt("level_$index", 0)
            if (savedLevel > 0) {
                up.level = savedLevel
                cps += up.cpsGain * up.level
                extraTapFromUpgrades += up.tapGain * up.level
            }
        }
    }

    // --- Ayarlar ---
    private fun refreshSettings() {
        vibrateEnabled = prefs.getBoolean("vibrate_enabled", true)
        soundEnabled   = prefs.getBoolean("sound_enabled", true)
        autosaveSec    = prefs.getInt("autosave_sec", 10).coerceIn(5, 120)
    }

    // --- Haptics & Sound (aynı) ---
    @SuppressLint("MissingPermission")
    private fun haptic() {
        if (!vibrateEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                val vm = getSystemService(VibratorManager::class.java)
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= 26) {
                    v.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION") v.vibrate(15)
                }
            }
        } catch (_: Throwable) {}
    }

    private fun initSound() {
        if (!soundEnabled) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attrs).build()
        try {
            soundClickId = soundPool?.load(this, R.raw.click, 1) ?: 0
            soundPool?.setOnLoadCompleteListener { _, _, status -> soundLoaded = (status == 0) }
        } catch (_: Throwable) {}
    }

    private fun playClickSound() {
        if (soundEnabled && soundLoaded) soundPool?.play(soundClickId, 1f, 1f, 1, 0, 1f)
    }

    private fun releaseSound() {
        soundPool?.release()
        soundPool = null
        soundLoaded = false
    }
    private fun spawnFlyingText(text: String, rawX: Float, rawY: Float) {
        val container = binding.flyingTextContainer

        val tv = layoutInflater.inflate(
            R.layout.view_flying_text,
            container,
            false
        ) as TextView

        tv.text = text
        if (text.startsWith("CRIT")) {
            tv.setTextColor(0xFFFF5252.toInt())
            tv.textSize = 26f
        }

        // Ekran koordinatını container koordinatına çevir
        val loc = IntArray(2)
        container.getLocationOnScreen(loc)
        val xInContainer = rawX - loc[0]
        val yInContainer = rawY - loc[1]

        // Başlangıç konumu
        tv.translationX = xInContainer - tv.paint.measureText(text) / 2
        tv.translationY = yInContainer - 40f
        tv.alpha = 1f

        container.addView(tv)

        // XML Animation yerine ViewPropertyAnimator (daha güvenli)
        tv.animate()
            .translationYBy(-160f)
            .alpha(0f)
            .setDuration(600L)
            .withEndAction {
                // draw sırasında child listesi değişmesin diye kaldırmayı post’la ertele
                container.post {
                    // parent kontrolü: iki kez kaldırılmaya çalışılırsa crash olmasın
                    if (tv.parent === container) {
                        container.removeView(tv)
                    }
                }
            }
            .start()
    }

}

// Model aynı
data class Upgrade(
    val id: Int,
    val title: String,
    val desc: String,
    val iconRes: Int,
    val basePrice: Double,
    val cpsGain: Double = 0.0,
    val tapGain: Int = 0,
    var level: Int = 0,
    val priceMultiplier: Double = 1.15
) {
    fun currentPrice(): Double = basePrice * Math.pow(priceMultiplier, level.toDouble())
}
