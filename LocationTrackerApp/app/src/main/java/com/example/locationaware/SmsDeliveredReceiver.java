package com.example.locationaware;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SmsDeliveredReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationAware";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Log.d(TAG, "SMS delivered successfully");
                Toast.makeText(context, "SMS Delivered", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.e(TAG, "SMS delivery failed, result code: " + getResultCode());
                Toast.makeText(context, "SMS Delivery Failed", Toast.LENGTH_SHORT).show();
                break;
        }
        // Unregister the receiver after handling the event
//        try {
//            context.unregisterReceiver(this);
//            Log.d(TAG, "deliveredReceiver unregistered");
//        } catch (Exception e) {
//            Log.e(TAG, "Error unregistering deliveredReceiver: " + e.getMessage());
//            // Receiver might not be registered or already unregistered
//        }

    }
}
