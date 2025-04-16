package com.example.sit2long

import com.google.android.gms.location.DetectedActivity

class DrivingState : ActivityState {
    override val type: Int = DetectedActivity.IN_VEHICLE
    override fun getActivityName(): String = "Driving"
}