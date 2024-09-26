package com.example.noms.ui.restaurants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RestaurantsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "noms Restaurant Screen"
    }
    val text: LiveData<String> = _text
}
