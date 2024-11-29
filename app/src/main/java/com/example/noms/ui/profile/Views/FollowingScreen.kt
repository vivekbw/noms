package com.example.noms.ui.profile.Views

import FollowerScreenViewModelFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.noms.backend.User
import com.example.noms.ui.profile.View.FollowerCard
import com.example.noms.ui.profile.ViewModels.FollowingScreenViewModel
import com.example.noms.ui.profile.ViewModels.FollowingScreenViewModelFactory

// Card for each follower
@Composable
fun FollowCard(navController: NavController, follower: User) {
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
    }
}

// Following screen, displays all users that the current user is following
@Composable
fun FollowingScreen(
    navController: NavController,
    currentUserId: Int,
) {
    val viewModel: FollowingScreenViewModel = viewModel(
        factory = FollowingScreenViewModelFactory()
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
                    FollowCard(
                        navController = navController,
                        follower = user,
                    )
                }
            }
        }
    }
}
