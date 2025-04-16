package com.example.sit2long

import com.google.android.gms.location.DetectedActivity

class SittingState : ActivityState {
    override val type: Int = DetectedActivity.STILL
    override fun getActivityName(): String = "Sitting"
}