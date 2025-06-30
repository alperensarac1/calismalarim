package com.example.isletimsistemlericalismasikotlin.uygulamalar.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentAlarmAnasayfaBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class AlarmAnasayfaFragment : Fragment() {

    private lateinit var binding: FragmentAlarmAnasayfaBinding
    private var timePicker: MaterialTimePicker? = null
    private var calendar: Calendar? = null
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    private var timer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmAnasayfaBinding.inflate(inflater, container, false)

        setupSelectTimeButton()
        setupSetAlarmButton()
        setupCancelAlarmButton()

        startTimer()

        return binding.root
    }

    private fun setupSelectTimeButton() {
        binding.selectTime.setOnClickListener {
            timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Alarm Zamanını Seçin")
                .build()

            timePicker?.addOnPositiveButtonClickListener {
                val formattedTime = formatSelectedTime()
                binding.selectTime.text = formattedTime

                calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timePicker!!.hour)
                    set(Calendar.MINUTE, timePicker!!.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }

            timePicker?.show(requireActivity().supportFragmentManager, "androidknowledge")
        }
    }

    private fun setupSetAlarmButton() {
        binding.setAlarm.setOnClickListener {
            if (calendar == null) {
                Toast.makeText(requireContext(), "Lütfen bir saat seçin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(requireContext(), AlarmReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

            alarmManager?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar!!.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent!!
            )

            Toast.makeText(requireContext(), "Alarm Ayarlandı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCancelAlarmButton() {
        binding.cancelAlarm.setOnClickListener {
            val intent = Intent(requireContext(), AlarmReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

            if (alarmManager == null) {
                alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            }

            alarmManager?.cancel(pendingIntent!!)
            Toast.makeText(requireContext(), "Alarm İptal Edildi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatSelectedTime(): String {
        val hour = timePicker?.hour ?: 0
        val minute = timePicker?.minute ?: 0
        val isPM = hour >= 12
        val formattedHour = when {
            hour > 12 -> hour - 12
            hour == 0 -> 12
            else -> hour
        }

        return String.format("%02d:%02d %s", formattedHour, minute, if (isPM) "PM" else "AM")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "akchannel"
            val desc = "Channel for Alarm Manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("androidknowledge", name, importance).apply {
                description = desc
            }

            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        createNotificationChannel()
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Bildirim İzni Gerekli")
                    .setMessage("Alarm bildirimlerini gösterebilmek için bu izne ihtiyaç var.")
                    .setPositiveButton("Tamam") { _, _ ->
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
                    }
                    .setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        } else {
            Toast.makeText(requireContext(), "Bildirim izni zaten verilmiş", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0) {
            val message = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                "Bildirim izni verildi"
            } else {
                "Bildirim izni reddedildi"
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentDateTime = getCurrentDateTime()
                requireActivity().runOnUiThread {
                    binding.tvGunSaat.text = currentDateTime
                }
            }
        }, 0, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
