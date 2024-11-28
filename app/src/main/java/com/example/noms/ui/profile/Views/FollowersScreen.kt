package com.example.noms.ui.profile.View

import FollowerCardViewModel
import FollowerScreenViewModel
import FollowerCardViewModelFactory
import FollowerScreenViewModelFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.noms.backend.User

@Composable
fun FollowerCard(navController: NavController, currUserId: Int, follower: User, viewModel: FollowerCardViewModel) {
    val isFollowing by viewModel.getIsFollowing(currUserId, follower.uid).collectAsState()
    viewModel.fetchFollowStatus(currUserId, follower.uid ?: 0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Add spacing between cards
            .border(
                width = 2.dp,
                color = Color(0xFF2E8B57), // Green border
                shape = RoundedCornerShape(50) // Rounded corners for the pill shape
            )
            .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(50)) // Light grey background for each card
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable {
                // Navigate to the playlist screen for the selected follower
                follower.uid?.let { navController.navigate("Playlist/$it") }
            }, // Add clickable to navigate
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // User's name
        Text(
            text = "${follower.first_name} ${follower.last_name}",
            style = MaterialTheme.typography.bodyMedium, // Slightly smaller text
            color = Color.Black // Text color for readability
        )

        // Follower indicator
        Box(
            modifier = Modifier
                .size(20.dp) // Smaller indicator
                .background(
                    color = if (isFollowing) Color.Green else Color.Red,
                    shape = CircleShape // Circular shape for the indicator
                )
        )
    }
}

@Composable
fun FollowersScreen(
    navController: NavController,
    currentUserId: Int,
) {
    val viewModel: FollowerScreenViewModel = viewModel(
        factory = FollowerScreenViewModelFactory()
    )
    val followerViewModel: FollowerCardViewModel = viewModel(
        factory = FollowerCardViewModelFactory()
    )

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredUsers by viewModel.filteredUsers.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        BasicTextField(
            value = searchQuery,
            onValueChange = {
                if (it != searchQuery) { // Only update if the query is different
                    viewModel.updateSearchQuery(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = Color(0xFF2E8B57),
                    shape = RoundedCornerShape(50)
                )
                .background(Color(0xFFc6c6c6), shape = RoundedCornerShape(50))
                .padding(12.dp),
            decorationBox = { innerTextField ->
                if (searchQuery.isEmpty()) {
                    Text("Search by first name", color = Color.Gray)
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(50.dp))

        // Enclosing LazyColumn with a grey pill-shaped background and green border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = Color(0xFF2E8B57),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredUsers.size) { index ->
                    val user = filteredUsers[index]

                    // Pass followerViewModel manually
                    FollowerCard(
                        navController = navController,
                        currUserId = currentUserId,
                        follower = user,
                        viewModel = followerViewModel
                    )
                }
            }
        }
    }
}




//
//@Preview(showBackground = true)
//@Composable
//fun FollowersScreenPreview() {
//    MaterialTheme {
//        FollowersScreen(currentUserId = 15)
//    }
//}
//
//@Composable
//fun FollowersScreen(currentUserId: Int) {
//    val coroutineScope = rememberCoroutineScope()
//    var users by remember { mutableStateOf(listOf<User>()) }
//    var searchQuery by remember { mutableStateOf("") }
//
//    // Fetch all users when the screen loads
//    LaunchedEffect(currentUserId) {
//        coroutineScope.launch {
//            users = getAllUsers()
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Search Bar
//        BasicTextField(
//            value = searchQuery,
//            onValueChange = { searchQuery = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .border(
//                    width = 2.dp,
//                    color = Color(0xFF2E8B57), // Green border
//                    shape = RoundedCornerShape(50) // Rounded corners for the pill shape
//                )
//                .background(Color(0xFFc6c6c6), shape = RoundedCornerShape(50))
//                .padding(12.dp),
//            decorationBox = { innerTextField ->
//                if (searchQuery.isEmpty()) {
//                    Text("Search by first name", color = Color.Gray)
//                }
//                innerTextField()
//            }
//        )
//
//        Spacer(modifier = Modifier.height(50.dp))
//
//        // Enclosing LazyColumn with a grey pill-shaped background and green border
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .border(
//                    width = 2.dp,
//                    color = Color(0xFF2E8B57), // Green border
//                    shape = RoundedCornerShape(16.dp)
//                )
//                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp)) // Grey background
//                .padding(16.dp) // Padding inside the enclosing box
//        ) {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//                contentPadding = PaddingValues(bottom = 64.dp) // Add padding to prevent overlap with the navbar
//            ) {
//                val filteredUsers = users.filter { it.first_name.contains(searchQuery, ignoreCase = true) }
//                items(filteredUsers.size) { index ->
//                    val user = filteredUsers[index]
//                    FollowerCard(currUserId = currentUserId, follower = user)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun FollowerCard(currUserId: Int, follower: User) {
//    var isFollowing by remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Check if the current user follows this follower
//    LaunchedEffect(follower.uid) {
//        follower.uid?.let { followeeId ->
//            coroutineScope.launch {
//                isFollowing = doesFollow(currUserId, followeeId)
//            }
//        }
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp) // Add spacing between cards
//            .border(
//                width = 2.dp,
//                color = Color(0xFF2E8B57), // Green border
//                shape = RoundedCornerShape(50) // Rounded corners for the pill shape
//            )
//            .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(50)) // Light grey background for each card
//            .padding(horizontal = 16.dp, vertical = 12.dp), // Adjust padding to make it smaller
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        // User's name
//        Text(
//            text = "${follower.first_name} ${follower.last_name}",
//            style = MaterialTheme.typography.bodyMedium, // Slightly smaller text
//            color = Color.Black // Text color for readability
//        )
//
//        // Follower indicator
//        Box(
//            modifier = Modifier
//                .size(20.dp) // Smaller indicator
//                .background(
//                    color = if (isFollowing) Color.Green else Color.Red,
//                    shape = CircleShape // Circular shape for the indicator
//                )
//        )
//    }
//}
//
//
//
//@Preview(showBackground = true)
//@Composable
//fun FollowerCardPreview() {
//    MaterialTheme {
//        // Simulate the preview of the FollowerCard with mock data
//        FollowerCard(
//            currUserId = 15, // the current user ID
//            //should be red
////            follower = User(
////                uid = 16,
////                first_name = "Joe",
////                last_name = "Baba",
////                phone_number = "+1 999-999-9999"
////            )
//
//            //should be green
//                    follower = User(
//                    uid = 10,
//            first_name = "Jack",
//            last_name = "Anderson",
//            phone_number = "+1 993-404-4438" // or null to test the red indicator
//        )
//        )
//    }
//}
