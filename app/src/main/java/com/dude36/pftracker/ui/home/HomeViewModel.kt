package com.dude36.pftracker.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        val now = Calendar.getInstance()
        if (now.get(Calendar.HOUR_OF_DAY) in 3..12) {
            value = "Good Morning"
        } else if (now.get(Calendar.HOUR_OF_DAY) in 12..18) {
            value = "Good Afternoon"
        } else if (now.get(Calendar.HOUR_OF_DAY) in 18..23) {
            value = "Good Evening"
        } else {
            value = "Good Night"
        }
    }
    val text: LiveData<String> = _text
}