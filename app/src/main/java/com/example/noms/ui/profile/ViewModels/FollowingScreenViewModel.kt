package com.example.noms.ui.profile.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.noms.backend.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FollowingScreenViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FollowingScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FollowingScreenViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class FollowingScreenViewModel() : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _users = MutableStateFlow<List<User>>(emptyList())

    val searchQuery: StateFlow<String> = _searchQuery
    val filteredUsers: StateFlow<List<User>> = combine(_users, _searchQuery) { users, query ->
        if (query.isBlank()) users else users.filter { it.first_name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Fetch all following users when the ViewModel is initialized
        viewModelScope.launch {
            _users.value = getFollowing(getCurrentUid())
            Log.d("Following", _users.value.size.toString())
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}