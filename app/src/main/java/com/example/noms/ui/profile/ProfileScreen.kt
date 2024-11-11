package com.example.noms.ui.profile

import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import com.example.noms.AuthComposeActivity
import com.example.noms.backend.User
import com.example.noms.backend.supabase
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val seaGreen = Color(0xFF2E8B57)
    var user by remember { mutableStateOf<User?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUser?.phoneNumber) {
        currentUser?.phoneNumber?.let { phone ->
            coroutineScope.launch {
                try {
                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
                    val result = supabase.from("users").select() {
                        filter {
                            eq("phone_number", formattedPhone)
                        }
                    }.decodeSingle<User>()
                    user = result
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                    Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp)
            .padding(start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = seaGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

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
            Text(
                text = "${user?.first_name ?: ""} ${user?.last_name ?: ""}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = user?.phone_number ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Following and Followers Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    navController.navigate("Followers")
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(36.dp)
                    .border(
                        width = 2.dp,
                        color = seaGreen,
                        shape = RoundedCornerShape(50)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Followers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = seaGreen,
                    maxLines = 1
                )
            }

            Button(
                onClick = {
                    navController.navigate("Following")
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(36.dp)
                    .border(
                        width = 2.dp,
                        color = seaGreen,
                        shape = RoundedCornerShape(50)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Following",
                    style = MaterialTheme.typography.bodyMedium,
                    color = seaGreen,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Add Playlist LazyColumn
        Text(
            text = "Restaurant Playlists",
            style = MaterialTheme.typography.titleMedium,
            color = seaGreen,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Allow LazyColumn to scroll within available space
        ) {
            RestaurantPlaylistScreenWithCards(uid = 15) // Replace with dynamic user ID if needed
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                context.startActivity(Intent(context, AuthComposeActivity::class.java))
                (context as? ComponentActivity)?.finish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .border(
                    width = 2.dp,
                    color = seaGreen,
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "Log Out",
                style = MaterialTheme.typography.titleSmall,
                color = seaGreen,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

//@Composable
//fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
//    val context = LocalContext.current
//    val auth = FirebaseAuth.getInstance()
//    val currentUser = auth.currentUser
//    val seaGreen = Color(0xFF2E8B57)
//    var user by remember { mutableStateOf<User?>(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//////    RestaurantPlaylistScreen(uid = 15)
////    RestaurantPlaylistScreenWithCards(uid = 15)
//
//    LaunchedEffect(currentUser?.phoneNumber) {
//        currentUser?.phoneNumber?.let { phone ->
//            coroutineScope.launch {
//                try {
//                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
//                    println("Searching for formatted phone: $formattedPhone")
//
//                    val result = supabase.from("users").select() {
//                        filter {
//                            eq("phone_number", formattedPhone)
//                        }
//                    }.decodeSingle<User>()
//                    user = result
//                } catch (e: Exception) {
//                    println("Error: ${e.message}")
//                    Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(bottom = 100.dp)
//            .padding(start = 16.dp, end = 16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Text(
//            text = "Profile",
//            style = MaterialTheme.typography.headlineMedium,
//            color = seaGreen,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier
//                .padding(vertical = 24.dp)
//        )
//
//        Box(
//            modifier = Modifier
//                .padding(bottom = 24.dp)
//                .size(80.dp)
//                .background(seaGreen, shape = CircleShape),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.Person,
//                contentDescription = "Profile Icon",
//                tint = Color.White,
//                modifier = Modifier.size(48.dp)
//            )
//        }
//
//        if (user != null) {
//            Text(
//                text = "${user?.first_name ?: ""} ${user?.last_name ?: ""}",
//                style = MaterialTheme.typography.titleLarge,
//                color = Color.Black,
//                modifier = Modifier
//                    .padding(bottom = 8.dp)
//            )
//
//            Text(
//                text = user?.phone_number ?: "",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier
//                    .padding(bottom = 24.dp)
//            )
//        } else {
//            Text(
//                text = "Loading...",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier
//                    .padding(bottom = 24.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Following and Followers Buttons
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 16.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Followers Button
//            Button(
//                onClick = {
//                    navController.navigate("Followers")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp) // Adjust height to make it compact
//                    .border(
//                        width = 2.dp,
//                        color = Color(0xFF2E8B57),
//                        shape = RoundedCornerShape(50) // Pill shape
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), // No fill
//                shape = RoundedCornerShape(50) // Pill shape
//            ) {
//                Text(
//                    text = "Followers",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color(0xFF2E8B57),
//                    maxLines = 1 // Prevent text overflow
//                )
//            }
//
//            // Following Button
//            Button(
//                onClick = {
//                    println("need to implement")
////                    navController.navigate("Following")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp) // Adjust height to make it compact
//                    .border(
//                        width = 2.dp,
//                        color = Color(0xFF2E8B57),
//                        shape = RoundedCornerShape(50) // Pill shape
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), // No fill
//                shape = RoundedCornerShape(50) // Pill shape
//            ) {
//                Text(
//                    text = "Following",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color(0xFF2E8B57),
//                    maxLines = 1 // Prevent text overflow
//                )
//            }
//        }
//
//
//        Spacer(modifier = Modifier.weight(1f))
//
//        Button(
//            onClick = {
//                FirebaseAuth.getInstance().signOut()
//                context.startActivity(Intent(context, AuthComposeActivity::class.java))
//                (context as? ComponentActivity)?.finish()
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 8.dp)
//                .border(
//                    width = 2.dp,
//                    color = seaGreen,
//                    shape = RoundedCornerShape(24.dp)
//                ),
//            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text(
//                "Log Out",
//                style = MaterialTheme.typography.titleSmall,
//                color = seaGreen,
//                modifier = Modifier
//                    .padding(vertical = 4.dp)
//            )
//        }
//    }
//}
