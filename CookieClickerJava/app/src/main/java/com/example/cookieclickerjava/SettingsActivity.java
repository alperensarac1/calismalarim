package com.example.cookieclickerjava;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cookieclickerjava.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding b;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.settings_title));
        }

        sp = getSharedPreferences("cookie_prefs", MODE_PRIVATE);

        // Load
        boolean vibrate = sp.getBoolean("vibrate_enabled", true);
        boolean sound = sp.getBoolean("sound_enabled", true);
        int autosave = clamp(sp.getInt("autosave_sec", 10), 5, 120);

        b.swVibrate.setChecked(vibrate);
        b.swSound.setChecked(sound);
        b.seekAutosave.setProgress(autosave);
        updateAutosaveText(autosave);

        // Save on change
        b.swVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean("vibrate_enabled", isChecked).apply();
            }
        });

        b.swSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean("sound_enabled", isChecked).apply();
            }
        });

        b.seekAutosave.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int v = clamp(progress, 5, 120);
                updateAutosaveText(v);
                if (fromUser) {
                    sp.edit().putInt("autosave_sec", v).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void updateAutosaveText(int v) {
        b.tvAutosaveValue.setText(getString(R.string.settings_autosave_value, v));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}