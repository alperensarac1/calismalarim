package com.example.cookieclickerjava;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cookieclickerjava.databinding.ActivityPrestigeShopBinding;

import java.util.ArrayList;
import java.util.List;

public class PrestigeShopActivity extends AppCompatActivity {

    private ActivityPrestigeShopBinding b;
    private SharedPreferences prefs;

    private int prestigePoints = 0;
    private List<PrestigePerk> perks; // sınıf seviyesi

    private Runnable onAdapterRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPrestigeShopBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.shop_title));
        }

        prefs = getSharedPreferences("cookie_prefs", MODE_PRIVATE);

        // State yükle
        prestigePoints = prefs.getInt("prestige_points", 0);

        // Context hazır, getString kullanılabilir
        perks = new ArrayList<>();
        perks.add(new PrestigePerk(
                "perk_gprod",
                getString(R.string.perk_gprod),
                getString(R.string.perk_gprod_desc, 5),
                1,
                1.6,
                0,
                Integer.MAX_VALUE
        ));
        perks.add(new PrestigePerk(
                "perk_crit",
                getString(R.string.perk_crit),
                getString(R.string.perk_crit_desc, 1, 3),
                2,
                1.7,
                0,
                Integer.MAX_VALUE
        ));
        perks.add(new PrestigePerk(
                "perk_discount",
                getString(R.string.perk_discount),
                getString(R.string.perk_discount_desc, 2, 50),
                3,
                1.8,
                0,
                25 // %50 tavan
        ));
        perks.add(new PrestigePerk(
                "perk_taptop",
                getString(R.string.perk_taptop),
                getString(R.string.perk_taptop_desc),
                2,
                1.5,
                0,
                Integer.MAX_VALUE
        ));

        // Kayıtlı seviyeleri yükle
        for (PrestigePerk perk : perks) {
            perk.setLevel(prefs.getInt(perk.getKey(), 0));
        }

        PrestigeShopAdapter adapter = new PrestigeShopAdapter(
                perks,
                cost -> prestigePoints >= cost,
                perk -> {
                    buyPerk(perk);
                }
        );

        b.rvPerks.setLayoutManager(new LinearLayoutManager(this));
        b.rvPerks.setAdapter(adapter);

        updateHeader();

        onAdapterRefresh = () -> {
            adapter.notifyDataSetChanged();
            updateHeader();
        };
    }

    private void updateHeader() {
        b.tvPoints.setText(getString(R.string.prestige_points, prestigePoints));
    }

    private void buyPerk(PrestigePerk perk) {
        int cost = perk.costForNext();
        if (prestigePoints < cost || perk.getLevel() >= perk.getMaxLevel()) return;

        prestigePoints -= cost;
        perk.setLevel(perk.getLevel() + 1);

        prefs.edit()
                .putInt("prestige_points", prestigePoints)
                .putInt(perk.getKey(), perk.getLevel())
                .apply();

        if (onAdapterRefresh != null) onAdapterRefresh.run();
    }
}