package com.example.cookieclickerjava;

import static java.lang.Double.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cookieclickerjava.databinding.ActivityMainBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity implements UpgradeAdapter.Listener {

    private ActivityMainBinding binding;
    private UpgradeAdapter adapter;

    private int prestigePoints = 0;

    // Perk seviyeleri
    private int perkGProd = 0;
    private int perkCrit = 0;
    private int perkDiscount = 0;
    private int perkTapTop = 0;
    private final int passiveCritMultiplier = 3;

    // --- Oyun durumu ---
    private double score = 0.0;
    private double cps = 0.0;
    private int baseTap = 1;
    private int extraTapFromUpgrades = 0;
    private int tapPower() { return baseTap + extraTapFromUpgrades; }


    private SharedPreferences prefs;
    private int prestigeLevel = 0;        // toplam prestij seviyesi
    private double prestigeMultiplier = 1.0; // üretime uygulanır (tap ve cps)

    private double totalMultiplier() {
        double prestige = 1.0 + (prestigeLevel * 0.10);
        double gprod = 1.0 + (perkGProd * 0.05);
        return prestige * gprod;
    }
    private double effectiveCps() { return cps * totalMultiplier(); }
    private int effectiveTap() { return (int) max(1, (tapPower() * totalMultiplier())); }

    // Ayarlar
    private boolean vibrateEnabled = true;
    private boolean soundEnabled = true;
    private int autosaveSec = 10;

    // Ses
    private SoundPool soundPool = null;
    private int soundClickId = 0;
    private boolean soundLoaded = false;

    // Döngüler
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long tickMs = 100L;

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            double eff = effectiveCps();
            if (eff > 0.0) {
                score += eff / (1000.0 / tickMs);
                updateUi();
            }
            handler.postDelayed(this, tickMs);
        }
    };

    private final Runnable autosaver = new Runnable() {
        @Override public void run() {
            saveState();
            handler.postDelayed(this, Math.max(5000L, autosaveSec * 1000L));
        }
    };

    // Crit
    private final int critMultiplier = 10;
    private final int critCooldownSec = 30;
    private boolean critReady = true;
    private int critCooldownLeft = 0;

    private final Runnable critTicker = new Runnable() {
        @Override public void run() {
            if (critCooldownLeft > 0) {
                critCooldownLeft--;
                updateCritUi();
                handler.postDelayed(this, 1000L);
            } else {
                critReady = true;
                updateCritUi();
            }
        }
    };

    // Yükseltmeler
    private final List<Upgrade> upgrades = new ArrayList<Upgrade>() {{
        add(new Upgrade(1, "Otomatik Tıklayıcı", "...", R.drawable.ic_upgrade_autoclicker, 50.0, 1.0, 0, 0, 1.15));
        add(new Upgrade(2, "Hızlı Karıştırıcı",  "...", R.drawable.ic_upgrade_mixer,       75.0, 0.0, 1, 0, 1.15));
        add(new Upgrade(3, "Minik Fırın",         "...", R.drawable.ic_upgrade_oven,       250.0, 5.0, 0, 0, 1.15));
        add(new Upgrade(4, "Çikolata Parçaları",  "...", R.drawable.ic_upgrade_choco,      300.0, 0.0, 3, 0, 1.15));
        add(new Upgrade(5, "Pastane",              "...", R.drawable.ic_upgrade_bakery,     1200.0, 25.0, 0, 0, 1.15));
        add(new Upgrade(6, "Fabrika",              "...", R.drawable.ic_upgrade_factory,    6000.0, 120.0, 0, 0, 1.15));
        add(new Upgrade(7, "Araştırma Lab.",       "...", R.drawable.ic_upgrade_lab,        8000.0, 0.0, 10, 0, 1.15));
        add(new Upgrade(8, "Roket Fırın",          "...", R.drawable.ic_upgrade_rocket,     42000.0, 750.0, 0, 0, 1.15));
    }};

    private final DecimalFormat dfScore = new DecimalFormat("#,###.##");
    private final DecimalFormat dfCps   = new DecimalFormat("#,###.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("cookie_prefs", MODE_PRIVATE);

        // Kısayol: Prestige'e uzun basınca Settings
        binding.btnPrestige.setOnLongClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        });

        loadState();
        refreshSettings();
        loadPrestige();
        initSound();

        setupRecycler();
        setupCookieClick();
        setupButtons();
        updateUi();

        // doOnLayout eşleniği
        binding.flyingTextContainer.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> { /* ready */ });
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(ticker);
        handler.postDelayed(autosaver, Math.max(5000L, autosaveSec * 1000L));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSettings();
        loadPrestige();
        updateUi();
        updateCritUi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(ticker);
        handler.removeCallbacks(autosaver);
        handler.removeCallbacks(critTicker);
        saveState();
        savePrestige();
        releaseSound();
    }

    // --- UI ---
    private void setupRecycler() {
        adapter = new UpgradeAdapter(upgrades, this);
        binding.rvUpgrades.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUpgrades.setAdapter(adapter);
        adapter.updateAffordability(score);
    }

    private void setupCookieClick() {
        final android.view.animation.Animation clickAnim =
                AnimationUtils.loadAnimation(this, R.anim.click_bounce);

        final float[] lastTouch = new float[2]; // [x, y]

        binding.btnCookie.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastTouch[0] = event.getRawX();
                lastTouch[1] = event.getRawY();
            }
            return false;
        });

        binding.btnCookie.setOnClickListener(v -> {
            int gain = effectiveTap();
            if (perkCrit > 0) {
                int chance = min(perkCrit, 100);
                int rnd = ThreadLocalRandom.current().nextInt(100);
                if (rnd < chance) {
                    gain *= passiveCritMultiplier;
                    spawnFlyingText("CRIT +" + gain, lastTouch[0], lastTouch[1]);
                } else {
                    spawnFlyingText("+" + gain, lastTouch[0], lastTouch[1]);
                }
            } else {
                spawnFlyingText("+" + gain, lastTouch[0], lastTouch[1]);
            }
            score += gain;
            v.startAnimation(clickAnim);
            haptic();
            playClickSound();
            updateUi();
        });
    }

    private void setupButtons() {
        binding.btnPrestige.setOnClickListener(v -> confirmPrestige());
        binding.btnCrit.setOnClickListener(v -> tryCritClick());
        binding.btnReset.setOnClickListener(v -> confirmReset());
        binding.btnShop.setOnClickListener(v ->
                startActivity(new Intent(this, PrestigeShopActivity.class))
        );
        updateCritUi();
    }

    private void updateCritUi() {
        binding.btnCrit.setEnabled(critReady);
        binding.btnCrit.setText(critReady ? getString(R.string.crit) : (critCooldownLeft + "s"));
    }

    private void updateUi() {
        binding.tvScore.setText(dfScore.format(score));
        String mult = String.format("x%.2f", totalMultiplier());
        binding.tvCps.setText(dfCps.format(effectiveCps()) + " / sn  (" + mult + ")");
        double discountPct = min(perkDiscount * 0.02, 0.50);
        adapter.updateAffordability(score, discountPct);
    }

    // --- UpgradeAdapter.Listener ---
    @Override
    public void onBuyClicked(@NonNull Upgrade item) {
        double discountPct = min(perkDiscount * 0.02, 0.50); // %50 tavan
        double price = item.currentPrice() * (1.0 - discountPct);
        if (score >= price) {
            score -= price;
            item.setLevel(item.getLevel() + 1);
            cps += item.getCpsGain();
            extraTapFromUpgrades += item.getTapGain();
            adapter.notifyItemChanged(upgrades.indexOf(item));
            updateUi();
        }
    }

    // --- Prestige ---
    private void loadPrestige() {
        prestigeLevel = prefs.getInt("prestige_level", 0);
        prestigeMultiplier = Math.max(1.0, (double) prefs.getFloat("prestige_mult", 1f));
        prestigePoints = prefs.getInt("prestige_points", 0);

        perkGProd = prefs.getInt("perk_gprod", 0);
        perkCrit = prefs.getInt("perk_crit", 0);
        perkDiscount = prefs.getInt("perk_discount", 0);
        perkTapTop = prefs.getInt("perk_taptop", 0);

        // Perk etkileri uygula
        baseTap = 1 + perkTapTop;
    }

    private void savePrestige() {
        prefs.edit()
                .putInt("prestige_level", prestigeLevel)
                .putFloat("prestige_mult", (float) prestigeMultiplier)
                .putInt("prestige_points", prestigePoints)
                .putInt("perk_gprod", perkGProd)
                .putInt("perk_crit", perkCrit)
                .putInt("perk_discount", perkDiscount)
                .putInt("perk_taptop", perkTapTop)
                .apply();
    }

    // Basit kazanç: sqrt(score / 1000). Her prestij seviyesi %10 çarpan verir.
    private int calcPrestigeGain(double currentScore) {
        double raw = sqrt(currentScore / 1000.0);
        return Math.max(0, (int) raw);
    }

    private void confirmPrestige() {
        int gain = calcPrestigeGain(score);
        if (gain <= 0) {
            binding.btnPrestige.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pop_in));
            return;
        }
        int percent = gain * 10;
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.prestige_confirm_title))
                .setMessage(getString(R.string.prestige_confirm_msg, percent))
                .setPositiveButton(getString(R.string.ok), (d, w) -> doPrestige(gain))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void doPrestige(int gain) {
        prestigeLevel += gain;
        prestigeMultiplier = 1.0 + (prestigeLevel * 0.10);

        prestigePoints += gain;
        savePrestige();

        // oyun reset (çarpan & perkler kalır)
        score = 0.0; cps = 0.0; extraTapFromUpgrades = 0;
        for (Upgrade u : upgrades) u.setLevel(0);
        saveState();
        updateUi();
        binding.btnPrestige.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pop_in));
    }

    // --- Crit ---
    private void tryCritClick() {
        if (!critReady) return;
        critReady = false;
        int gain = effectiveTap() * critMultiplier;
        score += gain;
        spawnFlyingText("+" + gain,
                binding.btnCookie.getX() + binding.btnCookie.getWidth() / 2f,
                binding.btnCookie.getY());
        haptic();
        playClickSound();
        updateUi();

        critCooldownLeft = critCooldownSec;
        updateCritUi();
        handler.post(critTicker);
    }

    // --- Reset ---
    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.reset_confirm_title))
                .setMessage(getString(R.string.reset_confirm_msg))
                .setPositiveButton(getString(R.string.ok), (d, w) -> doFullReset())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void doFullReset() {
        // Prestige ÇARPANI KALIR
        score = 0.0;
        cps = 0.0;
        extraTapFromUpgrades = 0;
        for (Upgrade u : upgrades) u.setLevel(0);

        // crit durumu
        critReady = true;
        critCooldownLeft = 0;
        handler.removeCallbacks(critTicker);

        saveState();
        updateUi();
        updateCritUi();
    }

    // --- Kalıcılık (oyun state) ---
    private void saveState() {
        SharedPreferences.Editor sp = getSharedPreferences("cookie_state", MODE_PRIVATE).edit();
        sp.putFloat("score", (float) score);
        sp.putFloat("cps", (float) cps);
        sp.putInt("extraTap", extraTapFromUpgrades);
        for (int i = 0; i < upgrades.size(); i++) {
            sp.putInt("level_" + i, upgrades.get(i).getLevel());
        }
        sp.apply();
    }

    private void loadState() {
        SharedPreferences sp = getSharedPreferences("cookie_state", MODE_PRIVATE);
        score = sp.getFloat("score", 0f);
        cps = sp.getFloat("cps", 0f);
        extraTapFromUpgrades = sp.getInt("extraTap", 0);
        for (int i = 0; i < upgrades.size(); i++) {
            int savedLevel = sp.getInt("level_" + i, 0);
            if (savedLevel > 0) {
                Upgrade up = upgrades.get(i);
                up.setLevel(savedLevel);
                cps += up.getCpsGain() * up.getLevel();
                extraTapFromUpgrades += up.getTapGain() * up.getLevel();
            }
        }
    }

    // --- Ayarlar ---
    private void refreshSettings() {
        vibrateEnabled = prefs.getBoolean("vibrate_enabled", true);
        soundEnabled   = prefs.getBoolean("sound_enabled", true);
        autosaveSec    = (int) max(5, min(120, prefs.getInt("autosave_sec", 10)));
    }

    // --- Haptics & Sound ---
    @SuppressLint("MissingPermission")
    private void haptic() {
        if (!vibrateEnabled) return;
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                VibratorManager vm = getSystemService(VibratorManager.class);
                if (vm != null) {
                    vm.getDefaultVibrator().vibrate(
                            VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
                    );
                }
            } else {
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        v.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        // noinspection deprecation
                        v.vibrate(15);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private void initSound() {
        if (!soundEnabled) return;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attrs).build();
        try {
            soundClickId = soundPool.load(this, R.raw.click, 1);
            soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundLoaded = (status == 0));
        } catch (Throwable ignored) {}
    }

    private void playClickSound() {
        if (soundEnabled && soundLoaded && soundPool != null) {
            soundPool.play(soundClickId, 1f, 1f, 1, 0, 1f);
        }
    }

    private void releaseSound() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        soundLoaded = false;
    }

    private void spawnFlyingText(@NonNull String text, float rawX, float rawY) {
        View container = binding.flyingTextContainer;

        TextView tv = (TextView) getLayoutInflater()
                .inflate(R.layout.view_flying_text, (ViewGroup) container, false);

        tv.setText(text);
        if (text.startsWith("CRIT")) {
            tv.setTextColor(0xFFFF5252);
            tv.setTextSize(26f);
        }

        // Ekran koordinatını container koordinatına çevir
        int[] loc = new int[2];
        container.getLocationOnScreen(loc);
        float xIn = rawX - loc[0];
        float yIn = rawY - loc[1];

        // Başlangıç konumu
        float textWidth = tv.getPaint().measureText(text);
        tv.setTranslationX(xIn - textWidth / 2f);
        tv.setTranslationY(yIn - 40f);
        tv.setAlpha(1f);

        ((ViewGroup) container).addView(tv);

        // ViewPropertyAnimator
        tv.animate()
                .translationYBy(-160f)
                .alpha(0f)
                .setDuration(600L)
                .withEndAction(() -> container.post(() -> {
                    if (tv.getParent() == container) {
                        ((ViewGroup) container).removeView(tv);
                    }
                }))
                .start();
    }
}