package com.waktu;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String PREF_NAME = "NovanPrefs";

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
        try {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean aktifJam = prefs.getBoolean("cbJam", true);
            boolean aktifMasehi = prefs.getBoolean("cbMasehi", true);
            boolean aktifHijriyah = prefs.getBoolean("cbHijriyah", true);
            boolean aktifBaterai = prefs.getBoolean("cbBaterai", true);

            StringBuilder pesanBuilder = new StringBuilder();

            // 1. Jam & Menit
            if (aktifJam) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                pesanBuilder.append("Pukul ").append(hour).append(" lewat ").append(minute).append(" menit. ");
            }

            // 2. Tanggal Masehi
            if (aktifMasehi) {
                SimpleDateFormat sdfMasehi = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID"));
                String tanggalMasehi = sdfMasehi.format(new Date());
                pesanBuilder.append("Tanggal Masehi ").append(tanggalMasehi).append(". ");
            }

            // 3. Tanggal Hijriyah (Akurat sistem Android)
            if (aktifHijriyah) {
                String tanggalHijriyah = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        HijrahDate hijriDate = HijrahDate.now();
                        long day = hijriDate.get(ChronoField.DAY_OF_MONTH);
                        long month = hijriDate.get(ChronoField.MONTH_OF_YEAR);
                        long year = hijriDate.get(ChronoField.YEAR);

                        String[] namaBulanHijriyah = {
                            "", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", 
                            "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban", 
                            "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
                        };
                        
                        String namaBulan = (month >= 1 && month <= 12) ? namaBulanHijriyah[(int)month] : "Bulan " + month;
                        tanggalHijriyah = day + " " + namaBulan + " " + year + " Hijriyah";
                    } catch (Exception e) {
                        tanggalHijriyah = "Hijriyah";
                    }
                } else {
                    tanggalHijriyah = "Hijriyah";
                }
                pesanBuilder.append("Tanggal Hijriyah ").append(tanggalHijriyah).append(". ");
            }

            // 4. Sisa Baterai
            if (aktifBaterai) {
                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                int batteryLevel = 0;
                if (bm != null) {
                    batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                }
                pesanBuilder.append("Sisa baterai ").append(batteryLevel).append(" persen.");
            }

            String pesanSuara = pesanBuilder.toString().trim();
            if (!pesanSuara.isEmpty() && tts != null) {
                tts.speak(pesanSuara, TextToSpeech.QUEUE_FLUSH, null, "VoiceID");
            }
        } catch (Exception e) {
            e.printStackTrace();
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