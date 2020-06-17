package com.dude36.pftracker.ui.entries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EntriesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Entries Fragment"
    }
    val text: LiveData<String> = _text
}