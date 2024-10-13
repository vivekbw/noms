package com.example.noms.ui.social

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.noms.ui.restaurants.UserList

@Composable
fun SocialScreen(innerPadding: PaddingValues) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        UserList()
    }
}