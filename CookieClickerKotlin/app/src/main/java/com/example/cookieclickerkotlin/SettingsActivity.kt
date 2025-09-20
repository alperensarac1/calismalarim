package com.example.cookieclickerkotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cookieclickerkotlin.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding
    private val sp by lazy { getSharedPreferences("cookie_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.title = getString(R.string.settings_title)

        // Load
        val vibrate = sp.getBoolean("vibrate_enabled", true)
        val sound = sp.getBoolean("sound_enabled", true)
        val autosave = sp.getInt("autosave_sec", 10).coerceIn(5, 120)

        b.swVibrate.isChecked = vibrate
        b.swSound.isChecked = sound
        b.seekAutosave.progress = autosave
        updateAutosaveText(autosave)

        // Save on change
        b.swVibrate.setOnCheckedChangeListener { _, checked ->
            sp.edit().putBoolean("vibrate_enabled", checked).apply()
        }
        b.swSound.setOnCheckedChangeListener { _, checked ->
            sp.edit().putBoolean("sound_enabled", checked).apply()
        }
        b.seekAutosave.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val v = progress.coerceIn(5, 120)
                updateAutosaveText(v)
                if (fromUser) sp.edit().putInt("autosave_sec", v).apply()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun updateAutosaveText(v: Int) {
        b.tvAutosaveValue.text = getString(R.string.settings_autosave_value, v)
    }
}
