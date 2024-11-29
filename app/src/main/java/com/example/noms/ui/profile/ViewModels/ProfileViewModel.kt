package com.example.noms.ui.profile.ViewModels
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

// ViewModel for the ProfileScreen
class ProfileScreenViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _allRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val allRestaurants: StateFlow<List<Restaurant>> get() = _allRestaurants

    private val _selectedRestaurants = MutableStateFlow<Set<Int>>(emptySet())
    val selectedRestaurants: StateFlow<Set<Int>> get() = _selectedRestaurants

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> get() = _showDialog

    private val _playlistName = MutableStateFlow("")
    val playlistName: StateFlow<String> get() = _playlistName

    init {
        fetchUser()
        fetchAllRestaurants()
    }

    // Fetch user data
    private fun fetchUser() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUid()
                _user.value = getUser(currentUserId)
            } catch (e: Exception) {
                println("Error fetching user data: ${e.message}")
            }
        }
    }

    // Fetch all restaurants
    private fun fetchAllRestaurants() {
        viewModelScope.launch {
            try {
                _allRestaurants.value = getAllRestaurants()
            } catch (e: Exception) {
                println("Error fetching restaurants: ${e.message}")
            }
        }
    }

    // Update playlist name
    fun updatePlaylistName(name: String) {
        _playlistName.value = name
    }

    // Toggle restaurant selection
    fun toggleRestaurantSelection(restaurantId: Int, isSelected: Boolean) {
        _selectedRestaurants.value = if (isSelected) {
            _selectedRestaurants.value + restaurantId
        } else {
            _selectedRestaurants.value - restaurantId
        }
    }

    // Show/hide dialog
    fun showDialog(show: Boolean) {
        _showDialog.value = show
        if (!show) {
            _selectedRestaurants.value = emptySet()
            _playlistName.value = ""
        }
    }

    // Create playlist
    fun createPlaylist(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (playlistName.value.isNotBlank()) {
                try {
                    val currentUserId = user.value?.uid ?: getCurrentUid()

                    // Create playlist
                    createPlaylist(playlistName.value, uid = currentUserId)

                    // Get new playlist ID and add restaurants
                    val playlistId = getPlaylistId(playlistName.value)
                    _selectedRestaurants.value.forEach { restaurantId ->
                        playlistId?.let { addRestaurantToPlaylist(restaurantId, it) }
                    }

                    onSuccess()
                    showDialog(false)
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error occurred")
                }
            } else {
                onError("Please enter a valid playlist name")
            }
        }
    }
}
