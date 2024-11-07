package com.example.noms.ui.followers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.noms.backend.User
import com.example.noms.backend.doesFollow
import com.example.noms.backend.getAllUsers
import com.example.noms.backend.getFollowers
import kotlinx.coroutines.launch

@Composable
fun FollowersScreen(currentUserId: Int) {
    val coroutineScope = rememberCoroutineScope()
    var users by remember { mutableStateOf(listOf<User>()) }
    var searchQuery by remember { mutableStateOf("") }

    // Fetch all users when the screen loads
    LaunchedEffect(currentUserId) {
        coroutineScope.launch {
            users = getAllUsers()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, MaterialTheme.shapes.medium)
                .padding(12.dp),
            decorationBox = { innerTextField ->
                if (searchQuery.isEmpty()) {
                    Text("Search by first name", color = Color.Gray)
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(50.dp))

        // List of all users
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val filteredUsers = users.filter { it.first_name.contains(searchQuery, ignoreCase = true) }
            items(filteredUsers.size) { index ->
                val user = filteredUsers[index]
                FollowerCard(currUserId = currentUserId, follower = user)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FollowersScreenPreview() {
    MaterialTheme {
        FollowersScreen(currentUserId = 15)
    }
}




@Composable
fun FollowerCard(currUserId: Int, follower: User) {
    var isFollowing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Check if the current user follows this follower
    LaunchedEffect(follower.uid) {
        follower.uid?.let { followeeId ->
            coroutineScope.launch {
                isFollowing = doesFollow(currUserId, followeeId)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // User's name
        Text(
            text = "${follower.first_name} ${follower.last_name}",
            style = MaterialTheme.typography.bodyLarge
        )

        // Follower indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isFollowing) Color.Green else Color.Red,
                    shape = MaterialTheme.shapes.small
                )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun FollowerCardPreview() {
    MaterialTheme {
        // Simulate the preview of the FollowerCard with mock data
        FollowerCard(
            currUserId = 15, // the current user ID
            //should be red
//            follower = User(
//                uid = 16,
//                first_name = "Joe",
//                last_name = "Baba",
//                phone_number = "+1 999-999-9999"
//            )

            //should be green
                    follower = User(
                    uid = 10,
            first_name = "Jack",
            last_name = "Anderson",
            phone_number = "+1 993-404-4438" // or null to test the red indicator
        )
        )
    }
}
