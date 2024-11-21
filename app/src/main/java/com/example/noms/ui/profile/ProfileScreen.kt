package com.example.noms.ui.profile

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.noms.AuthComposeActivity
import com.example.noms.backend.Restaurant
import com.example.noms.backend.User
import com.example.noms.backend.addRestaurantToPlaylist
import com.example.noms.backend.createPlaylist
import com.example.noms.backend.getAllRestaurants
import com.example.noms.backend.getPlaylistId
import com.example.noms.backend.supabase
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

//@Composable
//fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
//    val context = LocalContext.current
//    val auth = FirebaseAuth.getInstance()
//    val currentUser = auth.currentUser
//    val seaGreen = Color(0xFF2E8B57)
//    var user by remember { mutableStateOf<User?>(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(currentUser?.phoneNumber) {
//        currentUser?.phoneNumber?.let { phone ->
//            coroutineScope.launch {
//                try {
//                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
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
//            modifier = Modifier.padding(vertical = 24.dp)
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
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            Text(
//                text = user?.phone_number ?: "",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//        } else {
//            Text(
//                text = "Loading...",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
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
//            Button(
//                onClick = {
//                    navController.navigate("Followers")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Followers",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//
//            Button(
//                onClick = {
//                    navController.navigate("Following")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Following",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(18.dp))
//
//        // Add Playlist LazyColumn
//        Text(
//            text = "Restaurant Playlists",
//            style = MaterialTheme.typography.titleMedium,
//            color = seaGreen,
//            modifier = Modifier
//                .align(Alignment.Start)
//                .padding(bottom = 8.dp)
//        )
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f) // Allow LazyColumn to scroll within available space
//        ) {
//            RestaurantPlaylistScreenWithCards(uid = 15) // Replace with dynamic user ID if needed
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
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
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//    }
//}



//almost works - cant see playlists
//@Composable
//fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
//    val context = LocalContext.current
//    val auth = FirebaseAuth.getInstance()
//    val currentUser = auth.currentUser
//    val seaGreen = Color(0xFF2E8B57)
//    var user by remember { mutableStateOf<User?>(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(currentUser?.phoneNumber) {
//        currentUser?.phoneNumber?.let { phone ->
//            coroutineScope.launch {
//                try {
//                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
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
//    // Wrap the entire content in a Column that is scrollable
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState()) // Add vertical scroll here
//            .padding(bottom = 100.dp)
//            .padding(start = 16.dp, end = 16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Text(
//            text = "Profile",
//            style = MaterialTheme.typography.headlineMedium,
//            color = seaGreen,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(vertical = 24.dp)
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
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            Text(
//                text = user?.phone_number ?: "",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//        } else {
//            Text(
//                text = "Loading...",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
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
//            Button(
//                onClick = {
//                    navController.navigate("Followers")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Followers",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//
//            Button(
//                onClick = {
//                    navController.navigate("Following")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Following",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(18.dp))
//
//        // Add Playlist LazyColumn
//        Text(
//            text = "Restaurant Playlists",
//            style = MaterialTheme.typography.titleMedium,
//            color = seaGreen,
//            modifier = Modifier
//                .align(Alignment.Start)
//                .padding(bottom = 8.dp)
//        )
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f) // Allow LazyColumn to scroll within available space
//        ) {
//            RestaurantPlaylistScreenWithCards(uid = 15) // Replace with dynamic user ID if needed
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
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
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//    }
//}


//THIS ONE WORKS
//@Composable
//fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
//    val context = LocalContext.current
//    val auth = FirebaseAuth.getInstance()
//    val currentUser = auth.currentUser
//    val seaGreen = Color(0xFF2E8B57)
//    var user by remember { mutableStateOf<User?>(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(currentUser?.phoneNumber) {
//        currentUser?.phoneNumber?.let { phone ->
//            coroutineScope.launch {
//                try {
//                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
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
//    // Handle orientation changes
//    val configuration = LocalConfiguration.current
//    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
//    Log.d("ProfileScreen", "Orientation: ${if (isLandscape) "Landscape" else "Portrait"}")
//
//    // Wrap the entire content in a Column
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(bottom = 100.dp)
//            .padding(start = 16.dp, end = 16.dp)
//            .verticalScroll(rememberScrollState()), // Add vertical scroll
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Text(
//            text = "Profile",
//            style = MaterialTheme.typography.headlineMedium,
//            color = seaGreen,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(vertical = 24.dp)
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
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            Text(
//                text = user?.phone_number ?: "",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//        } else {
//            Text(
//                text = "Loading...",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
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
//            Button(
//                onClick = {
//                    navController.navigate("Followers")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Followers",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//
//            Button(
//                onClick = {
//                    navController.navigate("Following")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Following",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(18.dp))
//
//        // Add Playlist LazyColumn
//        Text(
//            text = "Restaurant Playlists",
//            style = MaterialTheme.typography.titleMedium,
//            color = seaGreen,
//            modifier = Modifier
//                .align(Alignment.Start)
//                .padding(bottom = 8.dp)
//        )
//
//        // Set a fixed or max height to constrain the LazyColumn
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .heightIn(max = 300.dp) // Set a max height
//                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp))
//                .border(
//                    width = 2.dp,
//                    color = seaGreen,
//                    shape = RoundedCornerShape(16.dp)
//                )
//        ) {
//            RestaurantPlaylistScreenWithCards(uid = 15) // Replace with dynamic user ID if needed
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
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
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//    }
//}package com.example.noms.ui.profile
//
//import android.content.Intent
//import android.content.res.Configuration
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.example.noms.AuthComposeActivity
//import com.example.noms.backend.User
//import com.example.noms.backend.createPlaylist
//import com.example.noms.backend.supabase
//import com.google.firebase.auth.FirebaseAuth
//import io.github.jan.supabase.postgrest.from
//import kotlinx.coroutines.launch
//
////@Composable
////fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
////    val context = LocalContext.current
////    val auth = FirebaseAuth.getInstance()
////    val currentUser = auth.currentUser
////    val seaGreen = Color(0xFF2E8B57)
////    var user by remember { mutableStateOf<User?>(null) }
////    val coroutineScope = rememberCoroutineScope()
////
////    LaunchedEffect(currentUser?.phoneNumber) {
////        currentUser?.phoneNumber?.let { phone ->
////            coroutineScope.launch {
////                try {
////                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
////                    val result = supabase.from("users").select() {
////                        filter {
////                            eq("phone_number", formattedPhone)
////                        }
////                    }.decodeSingle<User>()
////                    user = result
////                } catch (e: Exception) {
////                    println("Error: ${e.message}")
////                    Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
////                }
////            }
////        }
////    }
////
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(bottom = 100.dp)
////            .padding(start = 16.dp, end = 16.dp),
////        horizontalAlignment = Alignment.CenterHorizontally,
////    ) {
////        Text(
////            text = "Profile",
////            style = MaterialTheme.typography.headlineMedium,
////            color = seaGreen,
////            fontWeight = FontWeight.SemiBold,
////            modifier = Modifier.padding(vertical = 24.dp)
////        )
////
////        Box(
////            modifier = Modifier
////                .padding(bottom = 24.dp)
////                .size(80.dp)
////                .background(seaGreen, shape = CircleShape),
////            contentAlignment = Alignment.Center
////        ) {
////            Icon(
////                imageVector = Icons.Default.Person,
////                contentDescription = "Profile Icon",
////                tint = Color.White,
////                modifier = Modifier.size(48.dp)
////            )
////        }
////
////        if (user != null) {
////            Text(
////                text = "${user?.first_name ?: ""} ${user?.last_name ?: ""}",
////                style = MaterialTheme.typography.titleLarge,
////                color = Color.Black,
////                modifier = Modifier.padding(bottom = 8.dp)
////            )
////
////            Text(
////                text = user?.phone_number ?: "",
////                style = MaterialTheme.typography.bodyLarge,
////                color = Color.Gray,
////                modifier = Modifier.padding(bottom = 24.dp)
////            )
////        } else {
////            Text(
////                text = "Loading...",
////                style = MaterialTheme.typography.bodyLarge,
////                color = Color.Gray,
////                modifier = Modifier.padding(bottom = 24.dp)
////            )
////        }
////
////        Spacer(modifier = Modifier.height(16.dp))
////
////        // Following and Followers Buttons
////        Row(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(vertical = 16.dp),
////            horizontalArrangement = Arrangement.SpaceEvenly,
////            verticalAlignment = Alignment.CenterVertically
////        ) {
////            Button(
////                onClick = {
////                    navController.navigate("Followers")
////                },
////                modifier = Modifier
////                    .wrapContentWidth()
////                    .height(36.dp)
////                    .border(
////                        width = 2.dp,
////                        color = seaGreen,
////                        shape = RoundedCornerShape(50)
////                    ),
////                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
////                shape = RoundedCornerShape(50)
////            ) {
////                Text(
////                    text = "Followers",
////                    style = MaterialTheme.typography.bodyMedium,
////                    color = seaGreen,
////                    maxLines = 1
////                )
////            }
////
////            Button(
////                onClick = {
////                    navController.navigate("Following")
////                },
////                modifier = Modifier
////                    .wrapContentWidth()
////                    .height(36.dp)
////                    .border(
////                        width = 2.dp,
////                        color = seaGreen,
////                        shape = RoundedCornerShape(50)
////                    ),
////                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
////                shape = RoundedCornerShape(50)
////            ) {
////                Text(
////                    text = "Following",
////                    style = MaterialTheme.typography.bodyMedium,
////                    color = seaGreen,
////                    maxLines = 1
////                )
////            }
////        }
////
////        Spacer(modifier = Modifier.height(18.dp))
////
////        // Add Playlist LazyColumn
////        Text(
////            text = "Restaurant Playlists",
////            style = MaterialTheme.typography.titleMedium,
////            color = seaGreen,
////            modifier = Modifier
////                .align(Alignment.Start)
////                .padding(bottom = 8.dp)
////        )
////        Box(
////            modifier = Modifier
////                .fillMaxWidth()
////                .weight(1f) // Allow LazyColumn to scroll within available space
////        ) {
////            RestaurantPlaylistScreenWithCards(uid = 15) // Replace with dynamic user ID if needed
////        }
////
////        Spacer(modifier = Modifier.height(20.dp))
////
////        Button(
////            onClick = {
////                FirebaseAuth.getInstance().signOut()
////                context.startActivity(Intent(context, AuthComposeActivity::class.java))
////                (context as? ComponentActivity)?.finish()
////            },
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(bottom = 8.dp)
////                .border(
////                    width = 2.dp,
////                    color = seaGreen,
////                    shape = RoundedCornerShape(24.dp)
////                ),
////            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
////            shape = RoundedCornerShape(8.dp)
////        ) {
////            Text(
////                "Log Out",
////                style = MaterialTheme.typography.titleSmall,
////                color = seaGreen,
////                modifier = Modifier.padding(vertical = 4.dp)
////            )
////        }
////    }
////}
//
//
//
////almost works - cant see playlists
////@Composable
////fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
////    val context = LocalContext.current
////    val auth = FirebaseAuth.getInstance()
////    val currentUser = auth.currentUser
////    val seaGreen = Color(0xFF2E8B57)
////    var user by remember { mutableStateOf<User?>(null) }
////    val coroutineScope = rememberCoroutineScope()
////
////    LaunchedEffect(currentUser?.phoneNumber) {
////        currentUser?.phoneNumber?.let { phone ->
////            coroutineScope.launch {
////                try {
////                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
////                    val result = supabase.from("users").select() {
////                        filter {
////                            eq("phone_number", formattedPhone)
////                        }
////                    }.decodeSingle<User>()
////                    user = result
////                } catch (e: Exception) {
////                    println("Error: ${e.message}")
////                    Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
////                }
////            }
////        }
////    }
////
////    // Wrap the entire content in a Column that is scrollable
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .verticalScroll(rememberScrollState()) // Add vertical scroll here
////            .padding(bottom = 100.dp)
////            .padding(start = 16.dp, end = 16.dp),
////        horizontalAlignment = Alignment.CenterHorizontally,
////    ) {
////        Text(
////            text = "Profile",
////            style = MaterialTheme.typography.headlineMedium,
////            color = seaGreen,
////            fontWeight = FontWeight.SemiBold,
////            modifier = Modifier.padding(vertical = 24.dp)
////        )
////
////        Box(
////            modifier = Modifier
////                .padding(bottom = 24.dp)
////                .size(80.dp)
////                .background(seaGreen, shape = CircleShape),
////            contentAlignment = Alignment.Center
////        ) {
////            Icon(
////                imageVector = Icons.Default.Person,
////                contentDescription = "Profile Icon",
////                tint = Color.White,
////                modifier = Modifier.size(48.dp)
////            )
////        }
////
////        if (user != null) {
////            Text(
////                text = "${user?.first_name ?: ""} ${user?.last_name ?: ""}",
////                style = MaterialTheme.typography.titleLarge,
////                color = Color.Black,
////                modifier = Modifier.padding(bottom = 8.dp)
////            )
////
////            Text(
////                text = user?.phone_number ?: "",
////                style = MaterialTheme.typography.bodyLarge,
////                color = Color.Gray,
////                modifier = Modifier.padding(bottom = 24.dp)
////            )
////        } else {
////            Text(
////                text = "Loading...",
////                style = MaterialTheme.typography.bodyLarge,
////                color = Color.Gray,
////                modifier = Modifier.padding(bottom = 24.dp)
////            )
////        }
////
////        Spacer(modifier = Modifier.height(16.dp))
////
////        // Following and Followers Buttons
////        Row(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(vertical = 16.dp),
////            horizontalArrangement = Arrangement.SpaceEvenly,
////            verticalAlignment = Alignment.CenterVertically
////        ) {
////            Button(
////                onClick = {
////                    navController.navigate("Followers")
////                },
////                modifier = Modifier
////                    .wrapContentWidth()
////                    .height(36.dp)
////                    .border(
////                        width = 2.dp,
////                        color = seaGreen,
////                        shape = RoundedCornerShape(50)
////                    ),
////                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
////                shape = RoundedCornerShape(50)
////            ) {
////                Text(
////                    text = "Followers",
////                    style = MaterialTheme.typography.bodyMedium,
////                    color = seaGreen,
////                    maxLines = 1
////                )
////            }
////
////            Button(
////                onClick = {
////                    navController.navigate("Following")
////                },
////                modifier = Modifier
////                    .wrapContentWidth()
////                    .height(36.dp)
////                    .border(
////                        width = 2.dp,
////                        color = seaGreen,
////                        shape = RoundedCornerShape(50)
////                    ),
////                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
////                shape = RoundedCornerShape(50)
////            ) {
////                Text(
////                    text = "Following",
////                    style = MaterialTheme.typography.bodyMedium,
////                    color = seaGreen,
////                    maxLines = 1
////                )
////            }
////        }
////
////        Spacer(modifier = Modifier.height(18.dp))
////
////        // Add Playlist LazyColumn
////        Text(
////            text = "Restaurant Playlists",
////            style = MaterialTheme.typography.titleMedium,
////            color = seaGreen,
////            modifier = Modifier
////                .align(Alignment.Start)
////                .padding(bottom = 8.dp)
////        )
////        Box(
////            modifier = Modifier
////                .fillMaxWidth()
////                .weight(1f) // Allow LazyColumn to scroll within available space
////        ) {
////            RestaurantPlaylistScreenWithCards(uid = 15) // Replace with dynamic user ID if needed
////        }
////
////        Spacer(modifier = Modifier.height(20.dp))
////
////        Button(
////            onClick = {
////                FirebaseAuth.getInstance().signOut()
////                context.startActivity(Intent(context, AuthComposeActivity::class.java))
////                (context as? ComponentActivity)?.finish()
////            },
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(bottom = 8.dp)
////                .border(
////                    width = 2.dp,
////                    color = seaGreen,
////                    shape = RoundedCornerShape(24.dp)
////                ),
////            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
////            shape = RoundedCornerShape(8.dp)
////        ) {
////            Text(
////                "Log Out",
////                style = MaterialTheme.typography.titleSmall,
////                color = seaGreen,
////                modifier = Modifier.padding(vertical = 4.dp)
////            )
////        }
////    }
////}
//
//
////THIS ONE WORKS
////@Composable
////fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
////    val context = LocalContext.current
////    val auth = FirebaseAuth.getInstance()
////    val currentUser = auth.currentUser
////    val seaGreen = Color(0xFF2E8B57)
////    var user by remember { mutableStateOf<User?>(null) }
////    val coroutineScope = rememberCoroutineScope()
////
////    LaunchedEffect(currentUser?.phoneNumber) {
////        currentUser?.phoneNumber?.let { phone ->
////            coroutineScope.launch {
////                try {
////                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
////                    val result = supabase.from("users").select() {
////                        filter {
////                            eq("phone_number", formattedPhone)
////                        }
////                    }.decodeSingle<User>()
////                    user = result
////                } catch (e: Exception) {
////                    println("Error: ${e.message}")
////                    Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
////                }
////            }
////        }
////    }
////
////    // Handle orientation changes
////    val configuration = LocalConfiguration.current
////    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
////    Log.d("ProfileScreen", "Orientation: ${if (isLandscape) "Landscape" else "Portrait"}")
////
////    // Wrap the entire content in a Column
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(bottom = 100.dp)
////            .padding(start = 16.dp, end = 16.dp)
////            .verticalScroll(rememberScrollState()), // Add vertical scroll
////        horizontalAlignment = Alignment.CenterHorizontally,
////    ) {
////        Text(
////            text = "Profile",
////            style = MaterialTheme.typography.headlineMedium,
////            color = seaGreen,
////            fontWeight = FontWeight.SemiBold,
////            modifier = Modifier.padding(vertical = 24.dp)
////        )
////
////        Box(
////            modifier = Modifier
////                .padding(bottom = 24.dp)
////                .size(80.dp)
////                .background(seaGreen, shape = CircleShape),
////            contentAlignment = Alignment.Center
////        ) {
////            Icon(
////                imageVector = Icons.Default.Person,
////                contentDescription = "Profile Icon",
////                tint = Color.White,
////                modifier = Modifier.size(48.dp)
////            )
////        }
////
////        if (user != null) {
////            Text(
////                text = "${user?.first_name ?: ""} ${user?.last_name ?: ""}",
////                style = MaterialTheme.typography.titleLarge,
////                color = Color.Black,
////                modifier = Modifier.padding(bottom = 8.dp)
////            )
////
////            Text(
////                text = user?.phone_number ?: "",
////                style = MaterialTheme.typography.bodyLarge,
////                color = Color.Gray,
////                modifier = Modifier.padding(bottom = 24.dp)
////            )
////        } else {
////            Text(
////                text = "Loading...",
////                style = MaterialTheme.typography.bodyLarge,
////                color = Color.Gray,
////                modifier = Modifier.padding(bottom = 24.dp)
////            )
////        }
////
////        Spacer(modifier = Modifier.height(16.dp))
////
////        // Following and Followers Buttons
////        Row(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(vertical = 16.dp),
////            horizontalArrangement = Arrangement.SpaceEvenly,
////            verticalAlignment = Alignment.CenterVertically
////        ) {
////            Button(
////                onClick = {
////                    navController.navigate("Followers")
////                },
////                modifier = Modifier
////                    .wrapContentWidth()
////                    .height(36.dp)
////                    .border(
////                        width = 2.dp,
////                        color = seaGreen,
////                        shape = RoundedCornerShape(50)
////                    ),
////                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
////                shape = RoundedCornerShape(50)
////            ) {
////                Text(
////                    text = "Followers",
////                    style = MaterialTheme.typography.bodyMedium,
////                    color = seaGreen,
////                    maxLines = 1
////                )
////            }
////
////            Button(
////                onClick = {
////                    navController.navigate("Following")
////                },
////                modifier = Modifier
////                    .wrapContentWidth()
////                    .height(36.dp)
////                    .border(
////                        width = 2.dp,
////                        color = seaGreen,
////                        shape = RoundedCornerShape(50)
////                    ),
////                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
////                shape = RoundedCornerShape(50)
////            ) {
////                Text(
////                    text = "Following",
////                    style = MaterialTheme.typography.bodyMedium,
////                    color = seaGreen,
////                    maxLines = 1
////                )
////            }
////        }
////
////        Spacer(modifier = Modifier.height(18.dp))
////
////        // Add Playlist LazyColumn
////        Text(
////            text = "Restaurant Playlists",
////            style = MaterialTheme.typography.titleMedium,
////            color = seaGreen,
////            modifier = Modifier
////                .align(Alignment.Start)
////                .padding(bottom = 8.dp)
////        )
////
////        // Set a fixed or max height to constrain the LazyColumn
////        Box(
////            modifier = Modifier
////                .fillMaxWidth()
////                .heightIn(max = 300.dp) // Set a max height
////                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp))
////                .border(
////                    width = 2.dp,
////                    color = seaGreen,
////                    shape = RoundedCornerShape(16.dp)
////                )
////        ) {
////            RestaurantPlaylistScreenWithCards(uid = 15) // Replace with dynamic user ID if needed
////        }
////
////        Spacer(modifier = Modifier.height(20.dp))
////
////        Button(
////            onClick = {
////                FirebaseAuth.getInstance().signOut()
////                context.startActivity(Intent(context, AuthComposeActivity::class.java))
////                (context as? ComponentActivity)?.finish()
////            },
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(bottom = 8.dp)
////                .border(
////                    width = 2.dp,
////                    color = seaGreen,
////                    shape = RoundedCornerShape(24.dp)
////                ),
////            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
////            shape = RoundedCornerShape(8.dp)
////        ) {
////            Text(
////                "Log Out",
////                style = MaterialTheme.typography.titleSmall,
////                color = seaGreen,
////                modifier = Modifier.padding(vertical = 4.dp)
////            )
////        }
////    }
////}
////
//
//@Composable
//fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
//    val context = LocalContext.current
//    val auth = FirebaseAuth.getInstance()
//    val currentUser = auth.currentUser
//    val seaGreen = Color(0xFF2E8B57)
//    var user by remember { mutableStateOf<User?>(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(currentUser?.phoneNumber) {
//        currentUser?.phoneNumber?.let { phone ->
//            coroutineScope.launch {
//                try {
//                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
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
//            .padding(start = 16.dp, end = 16.dp)
//            .verticalScroll(rememberScrollState()),
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Text(
//            text = "Profile",
//            style = MaterialTheme.typography.headlineMedium,
//            color = seaGreen,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(vertical = 24.dp)
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
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            Text(
//                text = user?.phone_number ?: "",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//        } else {
//            Text(
//                text = "Loading...",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
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
//            Button(
//                onClick = {
//                    navController.navigate("Followers")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Followers",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//
//            Button(
//                onClick = {
//                    navController.navigate("Following")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Following",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(18.dp))
//
//        // Add Playlist Button
//        Button(
//            onClick = {
//                coroutineScope.launch {
//                    try {
//                        // Create a new playlist with the name "My Playlist" for user with UID = 15
//                        createPlaylist("My Playlist", uid = 15)
//                        Toast.makeText(context, "Playlist created!", Toast.LENGTH_SHORT).show()
//                    } catch (e: Exception) {
//                        Toast.makeText(context, "Error creating playlist: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//                .border(
//                    width = 2.dp,
//                    color = seaGreen,
//                    shape = RoundedCornerShape(24.dp)
//                ),
//            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text(
//                text = "Create Playlist",
//                style = MaterialTheme.typography.titleSmall,
//                color = seaGreen,
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//
//        // Restaurant Playlists Section
//        Text(
//            text = "Restaurant Playlists",
//            style = MaterialTheme.typography.titleMedium,
//            color = seaGreen,
//            modifier = Modifier
//                .align(Alignment.Start)
//                .padding(bottom = 8.dp)
//        )
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .heightIn(max = 300.dp)
//                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp))
//                .border(
//                    width = 2.dp,
//                    color = seaGreen,
//                    shape = RoundedCornerShape(16.dp)
//                )
//        ) {
//            RestaurantPlaylistScreenWithCards(uid = 15)
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        // Log Out Button
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
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//    }
//}
//


//works but cant add rest
//@Composable
//fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
//    val context = LocalContext.current
//    val auth = FirebaseAuth.getInstance()
//    val currentUser = auth.currentUser
//    val seaGreen = Color(0xFF2E8B57)
//    var user by remember { mutableStateOf<User?>(null) }
//    var showDialog by remember { mutableStateOf(false) } // Track if the dialog should be shown
//    var playlistName by remember { mutableStateOf("") }  // Store the input playlist name
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(currentUser?.phoneNumber) {
//        currentUser?.phoneNumber?.let { phone ->
//            coroutineScope.launch {
//                try {
//                    val formattedPhone = phone.replace(Regex("(\\+\\d)(\\d{3})(\\d{3})(\\d{4})"), "$1 $2-$3-$4")
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
//            .padding(start = 16.dp, end = 16.dp)
//            .verticalScroll(rememberScrollState()),
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Text(
//            text = "Profile",
//            style = MaterialTheme.typography.headlineMedium,
//            color = seaGreen,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(vertical = 24.dp)
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
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            Text(
//                text = user?.phone_number ?: "",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//        } else {
//            Text(
//                text = "Loading...",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
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
//            Button(
//                onClick = {
//                    navController.navigate("Followers")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Followers",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//
//            Button(
//                onClick = {
//                    navController.navigate("Following")
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .height(36.dp)
//                    .border(
//                        width = 2.dp,
//                        color = seaGreen,
//                        shape = RoundedCornerShape(50)
//                    ),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                shape = RoundedCornerShape(50)
//            ) {
//                Text(
//                    text = "Following",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = seaGreen,
//                    maxLines = 1
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(18.dp))
//
//        // Create Playlist Button
//        Button(
//            onClick = {
//                showDialog = true  // Show the dialog when clicked
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//                .border(
//                    width = 2.dp,
//                    color = seaGreen,
//                    shape = RoundedCornerShape(24.dp)
//                ),
//            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text(
//                text = "Create Playlist",
//                style = MaterialTheme.typography.titleSmall,
//                color = seaGreen,
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//
//        // Show the dialog when showDialog is true
//        if (showDialog) {
//            AlertDialog(
//                onDismissRequest = { showDialog = false },
//                title = {
//                    Text(text = "Enter Playlist Name")
//                },
//                text = {
//                    TextField(
//                        value = playlistName,
//                        onValueChange = { playlistName = it },
//                        label = { Text("Playlist Name") },
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                },
//                confirmButton = {
//                    Button(
//                        onClick = {
//                            if (playlistName.isNotBlank()) {
//                                coroutineScope.launch {
//                                    try {
//                                        // Create a new playlist with the entered name for user with UID = 15
//                                        createPlaylist(playlistName, uid = 15)
//                                        Toast.makeText(context, "Playlist created!", Toast.LENGTH_SHORT).show()
//                                        playlistName = "" // Reset the playlist name input field
//                                        showDialog = false  // Close the dialog after creation
//                                    } catch (e: Exception) {
//                                        Toast.makeText(context, "Error creating playlist: ${e.message}", Toast.LENGTH_SHORT).show()
//                                    }
//                                }
//                            } else {
//                                Toast.makeText(context, "Please enter a valid playlist name", Toast.LENGTH_SHORT).show()
//                            }
//                        },
//                        colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
//                    ) {
//                        Text("Create")
//                    }
//                },
//                dismissButton = {
//                    Button(
//                        onClick = { showDialog = false },  // Close the dialog without doing anything
//                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
//                    ) {
//                        Text("Cancel")
//                    }
//                }
//            )
//        }
//
//        // Restaurant Playlists Section
//        Text(
//            text = "Restaurant Playlists",
//            style = MaterialTheme.typography.titleMedium,
//            color = seaGreen,
//            modifier = Modifier
//                .align(Alignment.Start)
//                .padding(bottom = 8.dp)
//        )
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .heightIn(max = 300.dp)
//                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp))
//                .border(
//                    width = 2.dp,
//                    color = seaGreen,
//                    shape = RoundedCornerShape(16.dp)
//                )
//        ) {
//            RestaurantPlaylistScreenWithCards(uid = 15)
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        // Log Out Button
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
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//    }
//}


//We can select restaurants now in the playlist to add
@Composable
fun ProfileScreen(navController: NavController, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val seaGreen = Color(0xFF2E8B57)
    var user by remember { mutableStateOf<User?>(null) }
    var showDialog by remember { mutableStateOf(false) } // Track if the dialog should be shown
    var playlistName by remember { mutableStateOf("") }  // Store the input playlist name
    var allRestaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) } // All available restaurants

    // Use an immutable set for selected restaurants
    var selectedRestaurants by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch user data
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

    // Fetch all restaurants
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                allRestaurants = getAllRestaurants() // Get all available restaurants
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching restaurants: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp)
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = seaGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Profile Image and User Details
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

        // Create Playlist Button
        Button(
            onClick = {
                showDialog = true  // Show the dialog when clicked
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(
                    width = 2.dp,
                    color = seaGreen,
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Create Playlist",
                style = MaterialTheme.typography.titleSmall,
                color = seaGreen,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Show the dialog when showDialog is true
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(text = "Enter Playlist Name")
                },
                text = {
                    Column {
                        // TextField for Playlist Name
                        TextField(
                            value = playlistName,
                            onValueChange = { playlistName = it },
                            label = { Text("Playlist Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display available restaurants for selection
                        Text(
                            text = "Select Restaurants to Add:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // List of checkboxes to select restaurants
                        LazyColumn(
                            modifier = Modifier
                                .height(200.dp) // Adjust as needed
                        ) {
                            items(allRestaurants) { restaurant: Restaurant ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    val isChecked = restaurant.rid?.let { selectedRestaurants.contains(it) } ?: false

                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            restaurant.rid?.let { rid ->
                                                selectedRestaurants = if (checked) {
                                                    selectedRestaurants + rid
                                                } else {
                                                    selectedRestaurants - rid
                                                }
                                            }
                                        }
                                    )
                                    Text(
                                        text = restaurant.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (playlistName.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        // Step 1: Create a new playlist with the entered name
                                        createPlaylist(playlistName, uid = user?.uid ?: 15)

                                        // Step 2: Get the newly created playlist ID
                                        val playlistId = getPlaylistId(playlistName)

                                        // Step 3: Add selected restaurants to the playlist
                                        selectedRestaurants.forEach { restaurantId ->
                                            if (playlistId != null) {
                                                addRestaurantToPlaylist(restaurantId, playlistId)
                                            }
                                        }

                                        Toast.makeText(context, "Playlist created and restaurants added!", Toast.LENGTH_SHORT).show()
                                        playlistName = "" // Reset the playlist name input field
                                        selectedRestaurants = emptySet() // Clear selected restaurants
                                        showDialog = false  // Close the dialog after creation
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error creating playlist: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Please enter a valid playlist name", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            selectedRestaurants = emptySet() // Clear selections when dialog is dismissed
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Restaurant Playlists Section
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
                .heightIn(max = 300.dp)
                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = seaGreen,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Replace with your actual composable that displays playlists
            RestaurantPlaylistScreenWithCards(uid = user?.uid ?: 15)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Log Out Button
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
