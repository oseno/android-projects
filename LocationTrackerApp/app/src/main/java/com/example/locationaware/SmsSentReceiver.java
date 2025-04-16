package com.example.locationaware;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.content.BroadcastReceiver;

public class SmsSentReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationAware";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Log.d(TAG, "SMS sent successfully");
                Toast.makeText(context, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.e(TAG, "SMS failed to send, result code: " + getResultCode());
                Toast.makeText(context, "SMS Failed to Send", Toast.LENGTH_SHORT).show();
                break;
        }
        // Unregister the receiver after handling the event
//        try {
//            context.unregisterReceiver(this);
//            Log.d(TAG, "sentReceiver unregistered");
//        } catch (Exception e) {
//            Log.e(TAG, "Error unregistering sentReceiver: " + e.getMessage());
//            // Receiver might not be registered or already unregistered
//        }
    }
}
