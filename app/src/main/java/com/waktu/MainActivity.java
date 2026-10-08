package com.waktu;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity {
    
    private Switch switchMaster;
    private Spinner spinnerInterval, spinnerTts, spinnerNadaAwal, spinnerNadaAkhir;
    private CheckBox cbJam, cbMasehi, cbHijriyah, cbBaterai;
    private static final String PREF_NAME = "NovanPrefs";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.MATCH_PARENT
        ));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Jam, Tanggal Hijriyah & Masehi Bicara");
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        box.addView(title);

        // 1. Switch Master
        switchMaster = new Switch(this);
        switchMaster.setText("\nAktifkan Pengingat Suara Otomatis");
        switchMaster.setTextSize(16);
        box.addView(switchMaster);

        // 2. Pilihan Interval
        TextView lblInterval = new TextView(this);
        lblInterval.setText("\nInterval Waktu Berbunyi:");
        lblInterval.setTextColor(Color.BLACK);
        box.addView(lblInterval);

        spinnerInterval = new Spinner(this);
        String[] pilihanInterval = {"Setiap 5 Menit", "Setiap 15 Menit", "Setiap 30 Menit", "Setiap 1 Jam"};
        ArrayAdapter<String> adapterInterval = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pilihanInterval);
        spinnerInterval.setAdapter(adapterInterval);
        box.addView(spinnerInterval);

        // 3. Pilihan Komponen Suara
        TextView lblKomponen = new TextView(this);
        lblKomponen.setText("\nInformasi yang Ingin Disebutkan:");
        lblKomponen.setTextColor(Color.BLACK);
        box.addView(lblKomponen);

        cbJam = new CheckBox(this);
        cbJam.setText("Sebutkan Jam & Menit");
        cbJam.setChecked(true);
        box.addView(cbJam);

        cbMasehi = new CheckBox(this);
        cbMasehi.setText("Sebutkan Tanggal Masehi");
        cbMasehi.setChecked(true);
        box.addView(cbMasehi);

        cbHijriyah = new CheckBox(this);
        cbHijriyah.setText("Sebutkan Tanggal Hijriyah");
        cbHijriyah.setChecked(true);
        box.addView(cbHijriyah);

        cbBaterai = new CheckBox(this);
        cbBaterai.setText("Sebutkan Sisa Baterai");
        cbBaterai.setChecked(true);
        box.addView(cbBaterai);

        // 4. Pengaturan Suara TTS
        TextView lblTts = new TextView(this);
        lblTts.setText("\nPilih Suara / Bahasa TTS:");
        lblTts.setTextColor(Color.BLACK);
        box.addView(lblTts);

        spinnerTts = new Spinner(this);
        String[] pilihanTts = {"Default Sistem (Indonesia)", "Google TTS (Bahasa Indonesia)"};
        ArrayAdapter<String> adapterTts = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pilihanTts);
        spinnerTts.setAdapter(adapterTts);
        box.addView(spinnerTts);

        // 5. Pengaturan Nada Awal
        TextView lblNadaAwal = new TextView(this);
        lblNadaAwal.setText("\nNada Awal (Sebelum Berbicara):");
        lblNadaAwal.setTextColor(Color.BLACK);
        box.addView(lblNadaAwal);

        spinnerNadaAwal = new Spinner(this);
        String[] pilihanNadaAwal = {"Tanpa Nada", "BEEP Pendek", "Chime / Bel Masuk"};
        ArrayAdapter<String> adapterNadaAwal = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pilihanNadaAwal);
        spinnerNadaAwal.setAdapter(adapterNadaAwal);
        box.addView(spinnerNadaAwal);

        // 6. Pengaturan Nada Akhir
        TextView lblNadaAkhir = new TextView(this);
        lblNadaAkhir.setText("\nNada Akhir (Setelah Selesai Berbicara):");
        lblNadaAkhir.setTextColor(Color.BLACK);
        box.addView(lblNadaAkhir);

        spinnerNadaAkhir = new Spinner(this);
        String[] pilihanNadaAkhir = {"Tanpa Nada", "BEEP Pendek", "Klik Selesai"};
        ArrayAdapter<String> adapterNadaAkhir = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pilihanNadaAkhir);
        spinnerNadaAkhir.setAdapter(adapterNadaAkhir);
        box.addView(spinnerNadaAkhir);

        // Load Preferensi yang tersimpan sebelumnya
        loadPreferences();

        // Tombol Tes Suara
        Button btnTest = new Button(this);
        btnTest.setText("Tes Suara Sekarang");
        btnTest.setBackgroundColor(Color.parseColor("#4CAF50"));
        btnTest.setTextColor(Color.WHITE);
        
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanPreferensi(); // Simpan dulu sebelum dites
                Intent serviceIntent = new Intent(MainActivity.this, NovanService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                Toast.makeText(MainActivity.this, "Memulai tes suara...", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout.LayoutParams testParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        testParams.setMargins(0, 30, 0, 10);
        btnTest.setLayoutParams(testParams);
        box.addView(btnTest);

        // Tombol Simpan & Terapkan Pengaturan
        Button btnSimpan = new Button(this);
        btnSimpan.setText("Terapkan Pengaturan Alarm");
        
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanPreferensi();
                if (switchMaster.isChecked()) {
                    long intervalMillis = getSelectedIntervalMillis(spinnerInterval.getSelectedItemPosition());
                    aturAlarmOtomatis(intervalMillis);
                } else {
                    batalkanAlarmOtomatis();
                }
            }
        });
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 20, 0, 40);
        btnSimpan.setLayoutParams(btnParams);
        box.addView(btnSimpan);

        scrollView.addView(box);
        setContentView(scrollView);
    }

    private void simpanPreferensi() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("master", switchMaster.isChecked());
        editor.putInt("interval", spinnerInterval.getSelectedItemPosition());
        editor.putBoolean("cbJam", cbJam.isChecked());
        editor.putBoolean("cbMasehi", cbMasehi.isChecked());
        editor.putBoolean("cbHijriyah", cbHijriyah.isChecked());
        editor.putBoolean("cbBaterai", cbBaterai.isChecked());
        editor.putInt("tts", spinnerTts.getSelectedItemPosition());
        editor.putInt("nadaAwal", spinnerNadaAwal.getSelectedItemPosition());
        editor.putInt("nadaAkhir", spinnerNadaAkhir.getSelectedItemPosition());
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        switchMaster.setChecked(prefs.getBoolean("master", false));
        spinnerInterval.setSelection(prefs.getInt("interval", 1));
        cbJam.setChecked(prefs.getBoolean("cbJam", true));
        cbMasehi.setChecked(prefs.getBoolean("cbMasehi", true));
        cbHijriyah.setChecked(prefs.getBoolean("cbHijriyah", true));
        cbBaterai.setChecked(prefs.getBoolean("cbBaterai", true));
        spinnerTts.setSelection(prefs.getInt("tts", 0));
        spinnerNadaAwal.setSelection(prefs.getInt("nadaAwal", 0));
        spinnerNadaAkhir.setSelection(prefs.getInt("nadaAkhir", 0));
    }

    private long getSelectedIntervalMillis(int position) {
        switch (position) {
            case 0: return 5 * 60 * 1000;
            case 1: return 15 * 60 * 1000;
            case 2: return 30 * 60 * 1000;
            case 3: return 60 * 60 * 1000;
            default: return 15 * 60 * 1000;
        }
    }

    private void aturAlarmOtomatis(long intervalMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NovanReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + intervalMillis,
                intervalMillis,
                pendingIntent
            );
            Toast.makeText(this, "Pengingat suara otomatis berhasil diaktifkan!", Toast.LENGTH_LONG).show();
        }
    }

    private void batalkanAlarmOtomatis() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NovanReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Pengingat suara otomatis dimatikan.", Toast.LENGTH_SHORT).show();
        }
    }
}