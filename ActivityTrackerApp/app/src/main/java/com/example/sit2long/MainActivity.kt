package com.example.sit2long

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity(), ActivityObserver {
    private lateinit var activitySubject: ActivitySubject
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var activityReceiver: BroadcastReceiver
    private var activityTrackingEnabled by mutableStateOf(false)
    private var isSitting by mutableStateOf(true) // Default to sitting
    private var countdownSeconds by mutableStateOf(0L) // Will be set by user input
    private var currentActivity by mutableStateOf("You’re Sitting") // Default to sitting
    private var coroutineJob: Job? = null
    private var userDefinedDurationSeconds by mutableStateOf(0L) // User-defined time
    private var notificationSent by mutableStateOf(false) // Track if notification was sent
    private val ACTIVITY_RECEIVER_ACTION = "com.example.sit2long.ACTIVITY_ACTION"
    private val confidenceThreshold = 50 // Custom threshold for activity confidence (0-100)

    private val activityStates = listOf(SittingState(), WalkingState(), RunningState(), DrivingState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activitySubject = ActivitySubject()
        notificationHelper = NotificationHelper(this)
        activitySubject.addObserver(this)

        setupActivityReceiver()

        setContent {
            Sit2LongUI(
                currentActivity = currentActivity,
                countdownSeconds = countdownSeconds,
                activityTrackingEnabled = activityTrackingEnabled,
                notificationSent = notificationSent,
                onStartTrackingClick = { checkPermissionsAndStartTracking() },
                userDefinedDurationSeconds = userDefinedDurationSeconds,
                onDurationChange = { newDuration ->
                    userDefinedDurationSeconds = newDuration
                    countdownSeconds = newDuration // Reset timer to user input
                }
            )
        }
    }

    private fun checkPermissionsAndStartTracking() {
        val permissions = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION // Added for better detection
        )
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == android.content.pm.PackageManager.PERMISSION_GRANTED }) {
            enableActivityUpdates()
        } else {
            requestPermissionsLauncher.launch(permissions)
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value == true }) {
            enableActivityUpdates()
        } else {
            currentActivity = "Please grant permissions to track activities!"
            Log.e("MainActivity", "Permissions not granted: $permissions")
        }
    }

    private fun setupActivityReceiver() {
        activityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (ActivityRecognitionResult.hasResult(intent)) {
                    val result = ActivityRecognitionResult.extractResult(intent)!!
                    val probableActivities = result.probableActivities
                    if (probableActivities.isEmpty()) {
                        Log.w("MainActivity", "Received ActivityRecognitionResult with no activities")
                        return
                    }

                    // Find the activity with the highest confidence
                    val mostLikelyActivity = probableActivities.maxByOrNull { it.confidence }
                    mostLikelyActivity?.let { activity ->
                        Log.d("MainActivity", "Detected activity: ${activity.type}, confidence: ${activity.confidence}")
                        val state = activityStates.find { it.type == activity.type }
                        if (state != null && activity.confidence >= confidenceThreshold) {
                            activitySubject.updateActivity(state)
                        } else if (activity.confidence < confidenceThreshold) {
                            Log.d("MainActivity", "Confidence below threshold ($confidenceThreshold), ignoring activity: ${activity.type}")
                            // If confidence is too low, assume sitting
                            activitySubject.updateActivity(SittingState())
                        }
                    }
                } else {
                    Log.e("MainActivity", "No ActivityRecognitionResult in intent")
                }
            }
        }
    }

    private fun enableActivityUpdates() {
        if (userDefinedDurationSeconds <= 0) {
            currentActivity = "Please enter a valid time (in seconds)!"
            Log.e("MainActivity", "Invalid duration: $userDefinedDurationSeconds")
            return
        }

        val intent = Intent(ACTIVITY_RECEIVER_ACTION)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try {
            ActivityRecognition.getClient(this)
                .requestActivityUpdates(3000, pendingIntent) // Update every 3 seconds
                .addOnSuccessListener {
                    activityTrackingEnabled = true
                    notificationSent = false // Reset notification state
                    countdownSeconds = userDefinedDurationSeconds // Start with user-defined time
                    startCountdownTimer()
                    Log.d("MainActivity", "Activity updates started with duration: $userDefinedDurationSeconds seconds")
                }
                .addOnFailureListener { e ->
                    currentActivity = "Oops, couldn’t start tracking! $e"
                    Log.e("MainActivity", "Failed to register activity updates: $e")
                }
        } catch (e: SecurityException) {
            currentActivity = "Permission denied for activity tracking!"
            Log.e("MainActivity", "SecurityException: $e")
        }

        registerReceiver(activityReceiver, IntentFilter(ACTIVITY_RECEIVER_ACTION), RECEIVER_NOT_EXPORTED)
    }

    private fun disableActivityUpdates() {
        val intent = Intent(ACTIVITY_RECEIVER_ACTION)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try {
            ActivityRecognition.getClient(this)
                .removeActivityUpdates(pendingIntent)
                .addOnSuccessListener {
                    activityTrackingEnabled = false
                    coroutineJob?.cancel()
                    countdownSeconds = userDefinedDurationSeconds // Reset timer
                    isSitting = true // Reset to default sitting state
                    Log.d("MainActivity", "Activity updates stopped")
                }
                .addOnFailureListener { e ->
                    currentActivity = "Oops, couldn’t stop tracking! $e"
                    Log.e("MainActivity", "Failed to unregister activity updates: $e")
                }
        } catch (e: SecurityException) {
            currentActivity = "Permission denied for stopping tracking!"
            Log.e("MainActivity", "SecurityException: $e")
        }

        unregisterReceiver(activityReceiver)
    }

    private fun startCountdownTimer() {
        coroutineJob?.cancel()
        coroutineJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && countdownSeconds > 0 && activityTrackingEnabled) {
                delay(TimeUnit.SECONDS.toMillis(1))
                if (isSitting) {
                    countdownSeconds--
                    Log.d("MainActivity", "Timer counting down: $countdownSeconds seconds remaining, isSitting: $isSitting")
                    if (countdownSeconds == 0L) {
                        notificationHelper.sendSittingTooLongNotification()
                        notificationSent = true // Mark notification as sent
                        activityTrackingEnabled = false // Stop tracking
                        countdownSeconds = userDefinedDurationSeconds // Reset timer
                        withContext(Dispatchers.Main) {
                            currentActivity = "You’re Sitting" // Reset UI
                        }
                        Log.d("MainActivity", "Notification sent, tracking stopped, timer reset to $userDefinedDurationSeconds seconds")
                    }
                } else {
                    countdownSeconds = userDefinedDurationSeconds // Reset timer if not sitting
                    activityTrackingEnabled = false // Stop tracking to re-enable button
                    Log.d("MainActivity", "Timer reset to $userDefinedDurationSeconds seconds and tracking stopped because user is not sitting")
                    withContext(Dispatchers.Main) {
                        currentActivity = "You’re ${currentActivity.split(" ")[1]}" // Maintain current activity (e.g., Walking)
                    }
                }
            }
        }
    }

    override fun onActivityChanged(state: ActivityState) {
        currentActivity = "You’re ${state.getActivityName()}"
        isSitting = state is SittingState
        Log.d("MainActivity", "onActivityChanged: isSitting updated to $isSitting, state: ${state.getActivityName()}")
    }

    override fun onSittingTimeUpdated(minutes: Long) {
        // Not used in this version
    }

    override fun onPause() {
        super.onPause()
        // Do NOT disable activity updates on pause to allow background operation
        Log.d("MainActivity", "App paused, activity tracking continues in background")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "App resumed, activityTrackingEnabled: $activityTrackingEnabled")
    }
}

@Composable
fun Sit2LongUI(
    currentActivity: String,
    countdownSeconds: Long,
    activityTrackingEnabled: Boolean,
    notificationSent: Boolean,
    onStartTrackingClick: () -> Unit,
    userDefinedDurationSeconds: Long,
    onDurationChange: (Long) -> Unit
) {
    var durationInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE1E9))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = currentActivity,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF69B4),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text(
            text = "Time until notification: $countdownSeconds secs",
            fontSize = 18.sp,
            color = Color(0xFFFF69B4),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = durationInput,
            onValueChange = { newValue ->
                durationInput = newValue
                val duration = newValue.toLongOrNull() ?: 0L
                onDurationChange(duration)
            },
            label = { Text("Enter sitting time (seconds)", color = Color(0xFFFF69B4)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            enabled = !activityTrackingEnabled, // Disable input while tracking
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF69B4),
                unfocusedBorderColor = Color(0xFFFFB6C1),
                focusedLabelColor = Color(0xFFFF69B4),
                unfocusedLabelColor = Color(0xFFFFB6C1),
                cursorColor = Color(0xFFFF69B4)
            )
        )

        Button(
            onClick = onStartTrackingClick,
            enabled = (!activityTrackingEnabled || notificationSent) && userDefinedDurationSeconds > 0, // Enable only if not tracking or notification sent
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1))
        ) {
            Text(
                text = "Start Tracking",
                color = Color.White
            )
        }
    }
}