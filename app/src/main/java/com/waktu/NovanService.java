package com.waktu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import androidx.annotation.Nullable;
import java.util.Calendar;
import java.util.Locale;

public class NovanService extends Service implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private static final String CHANNEL_ID = "NovanServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Membuat Foreground Service agar tidak dimatikan sistem Android
        createNotificationChannel();
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Waktu Bicara Aktif")
                    .setContentText("Berjalan di latar belakang untuk mengumumkan waktu.")
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .build();
        }
        startForeground(1, notification);

        // Proses Baca Waktu & Baterai
        bacaWaktuDanBaterai();

        return START_STICKY;
    }

    private void bacaWaktuDanBaterai() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batteryLevel = 0;
        if (bm != null) {
            batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        final String pesanSuara = "Pukul " + hour + " lewat " + minute + " menit. Sisa baterai " + batteryLevel + " persen.";

        if (tts != null) {
            tts.speak(pesanSuara, TextToSpeech.QUEUE_FLUSH, null, "VoiceID");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(new Locale("id", "ID"));
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Novan Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @IBinder
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}