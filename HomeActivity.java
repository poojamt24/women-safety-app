package com.example.womensafetyapp;

import static android.Manifest.permission.RECORD_AUDIO;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Imports remain the same

public class HomeActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude;
    private double longitude;
    private PanicService panicService;

    private static final String KEY_SERVICE_RUNNING = "service_running";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private static String recordFile = null;
    private static final String LOG_TAG = "AudioRecording";
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private String storedPanicOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        panicService = new PanicService(getApplicationContext());
        TextView panicmodetext = findViewById(R.id.panicmodetext);

        // SharedPreferences initialization
        sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        storedPanicOption = sharedPreferences.getString("panicoption", "voiceRecord");
        editor.putBoolean("isServiceRunning", false);
        editor.apply();

        if (!sharedPreferences.getBoolean("isServiceRunning", false)) {
            panicmodetext.setText("Start Panic Mode");
        } else {
            panicmodetext.setText("Stop Panic Mode");
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            int LOCATION_PERMISSION_REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set up click listeners
        findViewById(R.id.relative).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RelativeActivity.class)));
        findViewById(R.id.emergencyno).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), EmergencyNoActivity.class)));
        findViewById(R.id.panicmodesetting).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), PanicSettingActivity.class)));
        findViewById(R.id.profile).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        findViewById(R.id.panicmode).setOnClickListener(v -> {
            if (panicmodetext.getText().equals("Start Panic Mode")) {
                if (storedPanicOption.equalsIgnoreCase("voiceRecord")) {
                    startRecording();
                    Toast.makeText(HomeActivity.this, "Voice recording started", Toast.LENGTH_SHORT).show();
                } else {
                    panicService.startPanicService();
                    Toast.makeText(HomeActivity.this, "Sms location feature enabled", Toast.LENGTH_SHORT).show();
                }
                panicmodetext.setText("Stop Panic Mode");
            } else {
                if (storedPanicOption.equalsIgnoreCase("voiceRecord")) {
                    stopRecording();
                    Toast.makeText(HomeActivity.this, "Voice recording stopped", Toast.LENGTH_SHORT).show();
                } else {
                    panicService.stopPanicService();
                    Toast.makeText(HomeActivity.this, "Sms location feature disabled", Toast.LENGTH_SHORT).show();
                }
                panicmodetext.setText("Start Panic Mode");
            }
        });

        // Notification setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("your_channel_id",
                    "Channel Name",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel Description");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "your_channel_id")
                .setSmallIcon(R.drawable.location)
                .setContentTitle("Women Safety App")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .addAction(R.drawable.panicmode, "Panic Mode", createButton1PendingIntent())
                .addAction(R.drawable.location, "Stop Panic Mode", createButton2PendingIntent());

        int notificationId = 1;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());

        ButtonClickReceiver receiver = new ButtonClickReceiver(panicService);
        IntentFilter filter = new IntentFilter();
        filter.addAction("BUTTON_1_ACTION");
        filter.addAction("BUTTON_2_ACTION");
        registerReceiver(receiver, filter);
    }

    // PendingIntent creation (with flags for API 31+)
    private PendingIntent createButton1PendingIntent() {
        Intent button1Intent = new Intent("BUTTON_1_ACTION");
        return PendingIntent.getBroadcast(this, 0, button1Intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent createButton2PendingIntent() {
        Intent button2Intent = new Intent("BUTTON_2_ACTION");
        return PendingIntent.getBroadcast(this, 0, button2Intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                onRestart();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean CheckPermissions() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION_CODE);
        onRestart();
    }

    public void recording(View view) {
        if (isRecording) {
            stopRecording();
            isRecording = false;
        } else {
            startRecording();
            isRecording = true;
        }
    }

    public void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Toast.makeText(getBaseContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public void startRecording() {
        String recordFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                + "/womensafetyapp";
        File directory = new File(recordFilePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.ENGLISH);
        Date current = new Date();
        recordFile = "Recording_" + format1.format(current) + ".3gp";
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordFilePath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRecording) {
            stopRecording();
        }
    }
}

class ButtonClickReceiver extends BroadcastReceiver {
    private PanicService panicService;

    public ButtonClickReceiver(PanicService panicService) {
        this.panicService = panicService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals("BUTTON_1_ACTION")) {
                if (!panicService.isPanicServiceRunning()) {
                    panicService.startPanicService();
                    ((HomeActivity) context).stopRecording();
                }
            } else if (intent.getAction().equals("BUTTON_2_ACTION")) {
                panicService.stopPanicService();
            }
        }
    }
}
