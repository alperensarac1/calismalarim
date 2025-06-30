package com.example.isletimsistemicalismasi.uygulamalar.alarm;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentAlarmAnasayfaBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class AlarmAnasayfaFragment extends Fragment {

    private FragmentAlarmAnasayfaBinding binding;
    private MaterialTimePicker timePicker;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Timer timer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmAnasayfaBinding.inflate(inflater, container, false);

        setupSelectTimeButton();
        setupSetAlarmButton();
        setupCancelAlarmButton();

        startTimer();

        return binding.getRoot();
    }

    private void setupSelectTimeButton() {
        binding.selectTime.setOnClickListener(view -> {
            timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Alarm Zamanını Seçin")
                    .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                String formattedTime = formatSelectedTime();
                binding.selectTime.setText(formattedTime);

                calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            });

            timePicker.show(requireActivity().getSupportFragmentManager(), "androidknowledge");
        });
    }

    private void setupSetAlarmButton() {
        binding.setAlarm.setOnClickListener(view -> {
            if (calendar == null) {
                Toast.makeText(requireContext(), "Lütfen bir saat seçin.", Toast.LENGTH_SHORT).show();
                return;
            }

            alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(requireContext(), AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );

            Toast.makeText(requireContext(), "Alarm Ayarlandı", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCancelAlarmButton() {
        binding.cancelAlarm.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager == null) {
                alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            }

            alarmManager.cancel(pendingIntent);
            Toast.makeText(requireContext(), "Alarm İptal Edildi", Toast.LENGTH_SHORT).show();
        });
    }

    private String formatSelectedTime() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        boolean isPM = hour >= 12;
        hour = hour > 12 ? hour - 12 : hour == 0 ? 12 : hour;

        return String.format("%02d:%02d %s", hour, minute, isPM ? "PM" : "AM");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "akchannel";
            String desc = "Channel for Alarm Manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("androidknowledge", name, importance);
            channel.setDescription(desc);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermission();
        createNotificationChannel();
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Bildirim İzni Gerekli")
                        .setMessage("Alarm bildirimlerini gösterebilmek için bu izne ihtiyaç var.")
                        .setPositiveButton("Tamam", (dialog, which) -> ActivityCompat.requestPermissions(requireActivity(),
                                new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0))
                        .setNegativeButton("Hayır", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
        } else {
            Toast.makeText(requireContext(), "Bildirim izni zaten verilmiş", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Bildirim izni verildi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Bildirim izni reddedildi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String currentDateTime = getCurrentDateTime();
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.tvGunSaat.setText(currentDateTime);
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
