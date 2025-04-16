package com.example.sit2long

import com.google.android.gms.location.DetectedActivity

class RunningState : ActivityState {
    override val type: Int = DetectedActivity.RUNNING
    override fun getActivityName(): String = "Running"
}