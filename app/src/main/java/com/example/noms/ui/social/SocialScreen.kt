package com.example.noms.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.noms.R
import android.util.Log
import com.example.noms.backend.*

@Composable
fun SocialScreen(innerPadding: PaddingValues) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("For you", "Following")

    var reviewPosts by remember { mutableStateOf<List<ReviewPost>>(emptyList()) }
    var followerReviewPosts by remember { mutableStateOf<List<ReviewPost>>(emptyList()) }

    // Fetch review posts asynchronously
    LaunchedEffect(Unit) {
        reviewPosts = recommendRestaurants()
        followerReviewPosts = followersRecommendedRestaurant(4)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F5))) {
        CustomTabRow(
            selectedTabIndex = selectedTab,
            tabs = tabs,
            onTabSelected = { selectedTab = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> ForYouTab(reviewPosts)
            1 -> ForYouTab(followerReviewPosts)
        }
    }
}

@Composable
fun CustomTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 90.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(36.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(36.dp))
            .background(Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            tabs.forEachIndexed { index, title ->
                CustomTab(
                    title = title,
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

@Composable
fun CustomTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(if (selected) Color(0xFFccd1d1) else Color(0xFFF5F5F5))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) Color.Black else Color(0xFF9E9E9E),
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ForYouTab(reviewPosts: List<ReviewPost>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(reviewPosts) { post ->
            ReviewCard(post)
        }
    }
}

@Composable
fun FollowingTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Following tab content")
    }
}

@Composable
fun ReviewCard(review: ReviewPost) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img1),
                    contentDescription = "Restaurant image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .blur(radius = 50.dp)
                            .background(
                                color = Color(0x60000000),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )

                    Text(
                        text = review.restaurantName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = review.reviewerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = getRatingColor(review.rating)
                    ) {
                        Text(
                            text = "${review.rating}/5",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getRatingColor(rating: Float): Color {
    return when {
        rating >= 4.0f -> Color(0xFF34C759)
        rating >= 3.0f -> Color(0xFFFFCC00)
        else -> Color(0xFFFF3B30)
    }
}
