package com.example.noms.ui.profile.Views

import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.noms.AuthComposeActivity
import com.example.noms.backend.Restaurant
import com.example.noms.backend.User
import com.example.noms.backend.addRestaurantToPlaylist
import com.example.noms.backend.createPlaylist
import com.example.noms.backend.getAllRestaurants
import com.example.noms.backend.getCurrentUid
import com.example.noms.backend.getPlaylistId
import com.example.noms.backend.getUser
import com.example.noms.ui.profile.ViewModels.ProfileScreenViewModel
import com.example.noms.ui.profile.ViewModels.ProfileScreenViewModelFactory
import com.example.noms.ui.profile.ViewModels.RestaurantPlaylistViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
    val playlistViewModel: RestaurantPlaylistViewModel = viewModel()
    val viewModel: ProfileScreenViewModel = viewModel(
        factory = ProfileScreenViewModelFactory(playlistViewModel)
    )

    val user by viewModel.user.collectAsState()
    val allRestaurants by viewModel.allRestaurants.collectAsState()
    val selectedRestaurants by viewModel.selectedRestaurants.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val playlistName by viewModel.playlistName.collectAsState()

    val context = LocalContext.current
    val seaGreen = Color(0xFF2E8B57)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp)
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = seaGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Profile Section
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .size(80.dp)
                .background(seaGreen, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Icon",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        if (user != null) {
            Text("${user?.first_name} ${user?.last_name}", style = MaterialTheme.typography.titleLarge)
            Text(user?.phone_number.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        } else {
            Text("Loading...", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("Followers") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, seaGreen)
            ) { Text("Search for friend", color = seaGreen) }

            Button(
                onClick = { navController.navigate("Following") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, seaGreen)
            ) { Text("Friends", color = seaGreen) }
        }
        // Restaurant Playlists Section
        Text(
            text = "Playlists",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF2E8B57),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RestaurantPlaylistScreenWithCards(
            uid = getCurrentUid(),
            viewModel = playlistViewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Create Playlist Button
        Button(
            onClick = { viewModel.showDialog(true) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, seaGreen)
        ) {
            Text("Create Playlist", color = seaGreen)
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(false) },
                title = { Text("Enter Playlist Name") },
                text = {
                    Column {
                        TextField(
                            value = playlistName,
                            onValueChange = viewModel::updatePlaylistName,
                            label = { Text("Playlist Name") }
                        )
                        LazyColumn {
                            items(allRestaurants) { restaurant ->
                                val isChecked = selectedRestaurants.contains(restaurant.rid)
                                Row {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            restaurant.rid?.let {
                                                viewModel.toggleRestaurantSelection(it, checked)
                                            }
                                        }
                                    )
                                    Text(restaurant.name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.createPlaylist(
                                onSuccess = {
                                    Toast.makeText(context, "Playlist created!", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    ) { Text("Create") }
                },
                dismissButton = {
                    Button(onClick = { viewModel.showDialog(false) }) { Text("Cancel") }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Log Out Button
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                context.startActivity(Intent(context, AuthComposeActivity::class.java))
                (context as? ComponentActivity)?.finish()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, seaGreen)
        ) {
            Text("Log Out", color = seaGreen)
        }
    }
}

