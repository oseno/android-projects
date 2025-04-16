package com.example.sit2long

class ActivitySubject {
    private val observers = mutableListOf<ActivityObserver>()
    private var currentState: ActivityState? = null
    private var sittingTimeMinutes: Long = 0

    fun addObserver(observer: ActivityObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: ActivityObserver) {
        observers.remove(observer)
    }

    fun updateActivity(state: ActivityState) {
        currentState = state
        observers.forEach { it.onActivityChanged(state) }
    }

    fun updateSittingTime(minutes: Long) {
        sittingTimeMinutes = minutes
        observers.forEach { it.onSittingTimeUpdated(minutes) }
    }
}