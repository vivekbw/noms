package com.example.noms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.noms.R
import com.example.noms.backend.*
import com.example.noms.ui.profile.View.FollowersScreen
import com.example.noms.ui.profile.Views.FollowingScreen
import com.example.noms.ui.profile.Views.ProfileScreen
import com.example.noms.ui.profile.Views.RestaurantPlaylistScreenWithCards
import com.example.noms.ui.restaurants.RestaurantDetailsScreen
import com.example.noms.ui.restaurants.RestaurantsScreen
import com.example.noms.ui.social.SocialScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val items = listOf(
        Triple("Restaurants", R.drawable.ic_restaurant_black_24dp, "Restaurants"),
        Triple("Social", R.drawable.ic_people_black_24dp, "Social"),
        Triple("Profile", R.drawable.ic_person_black_24dp, "Profile")
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("noms") },
                colors = topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .height(64.dp)
                ) {
                    NavigationBar(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        items.forEach { (route, icon, label) ->
                            NavigationBarItem(
                                icon = { Icon(painter = painterResource(id = icon), contentDescription = label) },
                                selected = currentRoute == route,
                                onClick = {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2E8B57),
                                    unselectedIconColor = Color.Black,
                                    indicatorColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "Restaurants",
            modifier = Modifier.padding(
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = 0.dp // Exclude bottom padding
                )
            )
        ) {
            composable("Restaurants") {
                RestaurantsScreen(navController, innerPadding)
            }
            composable("Social") {
                SocialScreen(innerPadding)
            }
            composable("Profile") {
                ProfileScreen(navController, innerPadding)
            }

            composable("Followers") {
                FollowersScreen(navController = navController, currentUserId = getCurrentUid()) // Pass the current user ID
            }

            composable("Following"){
                FollowingScreen(navController = navController, currentUserId = getCurrentUid()) // Pass the current user ID
            }

            composable(
                "Playlist/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                RestaurantPlaylistScreenWithCards(uid = userId) // Pass the user ID to the playlist screen
            }

            composable("RestaurantPlaylists") {
                RestaurantPlaylistScreenWithCards(uid = getCurrentUid()) // Replace with dynamic user ID
            }

            composable(
                "restaurantDetails/{placeId}",
                arguments = listOf(navArgument("placeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val placeId = backStackEntry.arguments?.getString("placeId") ?: ""

                RestaurantDetailsContent(placeId)
            }
        }
    }
}

@Composable
fun RestaurantDetailsContent(placeId: String) {
    val restaurant = remember { mutableStateOf<Restaurant?>(null) }

    LaunchedEffect(placeId) {
        val allRestaurants = getAllRestaurants() // Fetch all restaurants from Supabase
        restaurant.value = allRestaurants.find { it.placeId == placeId }
    }

    restaurant.value?.let { restaurant ->
        RestaurantDetailsScreen(restaurant) { review, rating ->
            println("Review for ${restaurant.name}: $review, Rating: $rating")
        }
    } ?: Text(
        text = "Loading...",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxSize(),
        textAlign = TextAlign.Center
    )
}
