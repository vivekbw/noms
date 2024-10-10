package com.example.noms

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class AuthComposeActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val bypassAuth = true // TODO: SET THIS TO TRUE, TO SKIP AUTH FOR TESTING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            AuthScreen(auth, bypassAuth)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthScreen(auth: FirebaseAuth, bypassAuth: Boolean) {
    val context = LocalContext.current
    val features = listOf(
        Pair(R.string.title_track, R.string.desc_track),
        Pair(R.string.title_share, R.string.desc_share),
        Pair(R.string.title_discover, R.string.desc_discover)
    )

    LaunchedEffect(auth) {
        if (auth.currentUser != null || bypassAuth) {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as? ComponentActivity)?.finish()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "noms",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF000000),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            DotsIndicator(
                totalDots = features.size,
                selectedIndex = 0
            )

            Spacer(modifier = Modifier.height(26.dp))

            GetStartedButton {
                if (bypassAuth) {
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                } else {
                    context.startActivity(Intent(context, PhoneAuthActivity::class.java))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LoginText {
                if (bypassAuth) {
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                } else {
                    context.startActivity(Intent(context, PhoneAuthActivity::class.java))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun rememberPagerState(initialPage: Int): PagerState {
//    return rememberPagerState(initialPage = initialPage)
//}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeatureCarousel(pagerState: PagerState, features: List<Pair<Int, Int>>) {
    HorizontalPager(
        state = pagerState,
        beyondBoundsPageCount = features.size,
        modifier = Modifier
            .height(150.dp)
            .width(width = 340.dp)
    ) { page ->
        FeatureItem(
            titleRes = features[page].first,
            descriptionRes = features[page].second
        )
    }
}

@Composable
fun FeatureItem(titleRes: Int, descriptionRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = titleRes),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E8B57)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = descriptionRes),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E8B57),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun DotsIndicator(totalDots: Int, selectedIndex: Int) {
    Row(
        modifier = Modifier
            .width(width = 340.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalDots) { index ->
            Dot(isSelected = index == selectedIndex)
            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun Dot(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFF2E8B57) else Color.LightGray)
    )
}

@Composable
fun GetStartedButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(width = 340.dp)
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B57))
    ) {
        Text("Get Started", color = Color.White)
    }
}

@Composable
fun LoginText(onClick: () -> Unit) {
    Text(
        text = "Already have an account? Log in",
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clickable(onClick = onClick),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF626262)
    )
}
