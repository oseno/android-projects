package com.example.sit2long

interface ActivityObserver {
    fun onActivityChanged(state: ActivityState)
    fun onSittingTimeUpdated(minutes: Long)
}