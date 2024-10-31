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

data class ReviewPost(
    val id: Int,
    val reviewerName: String,
    val restaurantName: String,
    val rating: Float,
    val comment: String
)

@Composable
fun SocialScreen(innerPadding: PaddingValues) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("For you", "Following")

    val reviewPosts = remember {
        listOf(
            ReviewPost(1, "John Doe", "Tasty Bites", 4.5f, "Great food and atmosphere! Highly recommended."),
            ReviewPost(2, "Jane Smith", "Burger Palace", 3.8f, "Decent burgers, but service was a bit slow."),
            ReviewPost(3, "Mike Johnson", "Sushi Haven", 5.0f, "Best sushi I've ever had! Will definitely come back."),
            ReviewPost(4, "Emily Brown", "Pizza Paradise", 4.2f, "Delicious pizza with a wide variety of toppings."),
            ReviewPost(5, "Chris Lee", "Taco Town", 2.5f, "Tacos were good, but a bit overpriced for the portion size.")
        )
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
            1 -> FollowingTab()
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
