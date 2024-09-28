package com.example.noms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.noms.R
import com.example.noms.ui.profile.ProfileScreen
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
        topBar = {
            TopAppBar(
                title = { Text("noms") },
                colors = topAppBarColors(
                    containerColor = Color(0x2E8B57)
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)  // This makes it take 60% of the screen width
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(45.dp))
                        .background(Color(0x2E8B57))
                        .height(64.dp),
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
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.Black,
                                indicatorColor = Color(0x2E8B57)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "Restaurants", Modifier.padding(innerPadding)) {
            composable("Restaurants") { RestaurantsScreen() }
            composable("Social") { SocialScreen() }
            composable("Profile") { ProfileScreen(navController) }
        }
    }
}
