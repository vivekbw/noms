package com.example.noms.ui.restaurants

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun RestaurantsScreen() {
    val waterloo = LatLng(43.4643, -80.5204)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(waterloo, 10f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = waterloo),
                title = "Waterloo",
                snippet = "Marker in Waterloo"
            )
        }
    }
}
