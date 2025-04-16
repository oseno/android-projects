package com.example.sit2long

import com.google.android.gms.location.DetectedActivity

class WalkingState : ActivityState {
    override val type: Int = DetectedActivity.WALKING
    override fun getActivityName(): String = "Walking"
}