package com.example.sit2long

interface ActivityState {
    val type: Int
    fun getActivityName(): String
}