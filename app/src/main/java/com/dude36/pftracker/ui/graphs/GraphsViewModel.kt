package com.dude36.pftracker.ui.graphs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GraphsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is a work in progress fragment"
    }
    val text: LiveData<String> = _text
}