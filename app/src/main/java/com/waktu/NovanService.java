package com.waktu;

import android.app.Service;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import androidx.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;

public class NovanService extends Service implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (tts != null) {
            bacaWaktuDanBaterai();
        }
        return START_STICKY;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(new Locale("id", "ID"));
            bacaWaktuDanBaterai();
        }
    }

    private void bacaWaktuDanBaterai() {
        // 1. Ambil Sisa Baterai
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batteryLevel = 0;
        if (bm != null) {
            batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }

        // 2. Ambil Jam, Menit, dan Tanggal Masehi
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        SimpleDateFormat sdfMasehi = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID"));
        String tanggalMasehi = sdfMasehi.format(new Date());

        // 3. Ambil Tanggal & Tahun Hijriyah dengan Koreksi
        String tanggalHijriyah = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            HijrahDate hijriDate = HijrahDate.now();
            
            // Diterapkan nilai koreksi +1 hari (bisa diubah menjadi -1 jika kelebihan)
            int koreksiHari = 1; 
            hijriDate = hijriDate.plusDays(koreksiHari);

            long day = hijriDate.get(ChronoField.DAY_OF_MONTH);
            long month = hijriDate.get(ChronoField.MONTH_OF_YEAR);
            long year = hijriDate.get(ChronoField.YEAR);

            // Konversi angka bulan hijriyah ke nama bulan dalam Bahasa Indonesia
            String[] namaBulanHijriyah = {
                "", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", 
                "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban", 
                "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
            };
            
            String namaBulan = (month >= 1 && month <= 12) ? namaBulanHijriyah[(int)month] : "Bulan " + month;
            tanggalHijriyah = day + " " + namaBulan + " " + year + " Hijriyah";
        } else {
            tanggalHijriyah = "Hijriyah tidak didukung versi Android ini";
        }

        // 4. Susun Pesan Suara Lengkap
        String pesanSuara = "Pukul " + hour + " lewat " + minute + " menit. " +
                "Tanggal Masehi " + tanggalMasehi + ". " +
                "Tanggal Hijriyah " + tanggalHijriyah + ". " +
                "Sisa baterai " + batteryLevel + " persen.";

        if (tts != null) {
            tts.speak(pesanSuara, TextToSpeech.QUEUE_FLUSH, null, "VoiceID");
        }
    }

    @Nullable
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