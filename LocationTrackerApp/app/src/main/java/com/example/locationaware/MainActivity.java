package com.example.locationaware;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.Manifest;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LocationAware"; // Tag for logging
    private static final int REQUEST_PERMISSIONS = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationFragment locationFragment;
    private LocationCallback locationCallback;
    private double latitude, longitude;
    // Instances of the new receiver classes
    private SmsSentReceiver sentReceiver;
    private SmsDeliveredReceiver deliveredReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate started");

        // Initialize receivers
        sentReceiver = new SmsSentReceiver();
        deliveredReceiver = new SmsDeliveredReceiver();

        // Get LocationFragment
        locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentById(R.id.locationFragment);
        Log.d(TAG, "LocationFragment retrieved: " + (locationFragment != null));

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d(TAG, "FusedLocationProviderClient initialized");

        // Set up buttons
        Button btnMapMe = findViewById(R.id.btnMapMe);
        Button btnTextMe = findViewById(R.id.btnTextMe);

        // Initialize location callback
        createLocationCallback();
        Log.d(TAG, "Location callback created");

        // Check for permissions and start location updates
        checkPermissionsAndStartLocationUpdates();
        Log.d(TAG, "Permission check initiated");

        // Set button click listeners
        btnMapMe.setOnClickListener(v -> {
            Log.d(TAG, "MapMe button clicked");
            showMap();
        });
        btnTextMe.setOnClickListener(v -> {
            Log.d(TAG, "TextMe button clicked");
            sendLocationSMS();
        });
        Log.i(TAG, "onCreate completed");
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLocations().get(0);
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d(TAG, "Location updated: Lat=" + latitude + ", Long=" + longitude);

                    if (locationFragment != null) {
                        locationFragment.updateCoordinates(latitude, longitude);
                        Log.d(TAG, "Fragment UI updated with new coordinates");
                    } else {
                        Log.w(TAG, "locationFragment is null, UI not updated");
                    }
                } else {
                    Log.w(TAG, "Location result received but locations list is empty");
                }
            }
        };
    }

    private void checkPermissionsAndStartLocationUpdates() {
        Log.d(TAG, "Checking permissions");
        boolean needFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean needSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED;
        boolean needPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "Permission status - FINE_LOCATION: " + !needFineLocation +
                ", SEND_SMS: " + !needSMS +
                ", READ_PHONE_STATE: " + !needPhoneState);

        if (needFineLocation || needSMS || needPhoneState) {
            Log.i(TAG, "Requesting permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.READ_PHONE_STATE
                    },
                    REQUEST_PERMISSIONS);
        } else {
            Log.i(TAG, "All permissions granted, starting location updates");
            startLocationUpdates();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) { // Fixed requestCode check
            boolean allGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    Log.w(TAG, "Permission denied: " + permissions[i]);
                } else {
                    Log.d(TAG, "Permission granted: " + permissions[i]);
                }
            }

            if (allGranted) {
                Log.i(TAG, "All permissions granted, starting location updates");
                startLocationUpdates();
            } else {
                Log.w(TAG, "Some permissions denied - app functionality limited");
                Toast.makeText(this, "Permissions denied - app functionality limited", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        Log.d(TAG, "Setting up location updates");
        LocationRequest locationRequest = new LocationRequest.Builder(1000)  // Update interval in milliseconds
                .setMinUpdateIntervalMillis(1000)  // Fastest update interval
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();
        Log.d(TAG, "LocationRequest created: interval=1000ms, priority=HIGH_ACCURACY");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Log.i(TAG, "Location updates requested successfully");
        } else {
            Log.e(TAG, "Cannot start location updates - FINE_LOCATION permission not granted");
        }
    }

    private void showMap() {
        Log.d(TAG, "showMap() called");

        if (latitude == 0 && longitude == 0) {
            Log.w(TAG, "Location not available yet (lat/long are 0)");
            Toast.makeText(this, "Location not available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Preparing to show map at coordinates: " + latitude + ", " + longitude);
        String uriString = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
        Uri gmmIntentUri = Uri.parse(uriString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void sendLocationSMS() {
        Log.d(TAG, "sendLocationSMS() called");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "SMS permission not granted");
            Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = "+250784803833";
        String message = "My location: Lat " + latitude + ", Long " + longitude;
        Log.d(TAG, "Preparing SMS with message: " + message);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            Log.d(TAG, "SmsManager obtained");
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

            // Use standard SMS intent actions
            Intent sentIntent = new Intent("SMS_SENT");
            Intent deliveredIntent = new Intent("SMS_DELIVERED");

            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent, flags);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, deliveredIntent, flags);

            Log.i(TAG, "Sending SMS to: " + phoneNumber);
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
            Log.d(TAG, "sendTextMessage called successfully");
            Toast.makeText(this, "Sending location via SMS...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Exception sending SMS: " + e.getMessage(), e);
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Override
protected void onResume() {
    Log.d(TAG, "onResume called");
    super.onResume();

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        startLocationUpdates();
        Log.d(TAG, "Location updates resumed");
    } else {
        Log.w(TAG, "Cannot resume location updates - permission not granted");
    }

    // Register receivers with standard SMS actions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        registerReceiver(sentReceiver, new IntentFilter("SMS_SENT"), RECEIVER_NOT_EXPORTED);
    } else {
        registerReceiver(sentReceiver, new IntentFilter("SMS_SENT"));
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        registerReceiver(deliveredReceiver, new IntentFilter("SMS_DELIVERED"), Context.RECEIVER_NOT_EXPORTED);
    }
    Log.d(TAG, "SMS receivers registered with standard actions");
}

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        super.onPause();

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Location updates removed");
        }

        try {
            unregisterReceiver(sentReceiver);
            unregisterReceiver(deliveredReceiver);
            Log.d(TAG, "SMS receivers unregistered");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering SMS receivers in onPause: " + e.getMessage());
        }
    }

}