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

/**
 * This is the factory for the follower screen view model.
 */

class FollowerScreenViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FollowerScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FollowerScreenViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * This is the factory for the follower card view model.
 */

class FollowerCardViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FollowerCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FollowerCardViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * This is the view model for the follower card.
 */

class FollowerCardViewModel() : ViewModel() {
    private val _followStatus = MutableStateFlow<Map<Pair<Int, Int>, Boolean>>(emptyMap())

    // Public getter to observe follow status for a specific user pair
    fun getIsFollowing(currUserId: Int, followerId: Int?): StateFlow<Boolean> {
        if (followerId == null) {
            // Return a flow with false if followerId is null
            return MutableStateFlow(false)
        }

        // Create a flow derived from the shared cache
        return _followStatus.map { cache ->
            cache[currUserId to followerId] ?: false // Default to false if not in cache
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // Always actively observe
            initialValue = false // Initial value for the flow
        )
    }

    // Function to fetch and update follow status
    fun fetchFollowStatus(currUserId: Int, followerId: Int) {
        viewModelScope.launch {
            val isFollowing = doesFollow(currUserId, followerId)
            _followStatus.update { it + ((currUserId to followerId) to isFollowing) } // Update cache
        }
    }
}

/**
 * This is the view model for the followers screen.
 */

class FollowerScreenViewModel() : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _users = MutableStateFlow<List<User>>(emptyList())

    val searchQuery: StateFlow<String> = _searchQuery
    val filteredUsers: StateFlow<List<User>> = combine(_users, _searchQuery) { users, query ->
        if (query.isBlank()) users else users.filter { it.first_name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Fetch all users when the ViewModel is initialized
        viewModelScope.launch {
            _users.value = getAllUsers()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
