package com.waktu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.speech.tts.TextToSpeech;
import java.util.Calendar;
import java.util.Locale;

public class NovanReceiver extends BroadcastReceiver {
    private TextToSpeech tts;

    @Override
    public void onReceive(final Context context, Intent intent) {
        // 1. Ambil Sisa Persentase Baterai
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = 0;
        if (bm != null) {
            batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }

        // 2. Ambil Jam & Menit Saat Ini
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 3. Susun Teks Pesan (Waktu & Baterai)
        final String pesanSuara = "Pukul " + hour + " lewat " + minute + " menit. Sisa baterai " + batteryLevel + " persen.";

        // 4. Jalankan Text-to-Speech
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("id", "ID"));
                    
                    // Ucapkan teks informasi waktu dan baterai secara otomatis
                    tts.speak(pesanSuara, TextToSpeech.QUEUE_FLUSH, null, "VoiceID");
                }
            }
        });
    }
}