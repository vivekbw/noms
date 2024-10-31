package com.example.noms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noms.backend.confirmUser
import com.example.noms.backend.createUser
import com.example.noms.components.PhoneNumberInput
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthComposeActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val bypassAuth = false // TODO: SET THIS TO TRUE, TO SKIP AUTH FOR TESTING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            AuthScreen(auth, bypassAuth)
        }
    }
}

@Composable
fun AuthScreen(auth: FirebaseAuth, bypassAuth: Boolean) {
    var currentScreen by remember { mutableStateOf(AuthScreen.INITIAL) }
    val context = LocalContext.current

    LaunchedEffect(auth.currentUser, bypassAuth) {
        if (auth.currentUser != null || bypassAuth) {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as? ComponentActivity)?.finish()
        }
    }

    when (currentScreen) {
        AuthScreen.INITIAL -> InitialScreen(
            onRegisterClick = { currentScreen = AuthScreen.REGISTER },
            onLoginClick = { currentScreen = AuthScreen.LOGIN }
        )
        AuthScreen.REGISTER -> RegistrationScreen(
            auth = auth,
            onBack = { currentScreen = AuthScreen.INITIAL },
            onNavigateToLogin = { currentScreen = AuthScreen.LOGIN }
        )
        AuthScreen.LOGIN -> LoginScreen(
            auth = auth,
            onBack = { currentScreen = AuthScreen.INITIAL }
        )
    }
}

enum class AuthScreen {
    INITIAL,
    REGISTER,
    LOGIN
}

@Composable
fun InitialScreen(onRegisterClick: () -> Unit, onLoginClick: () -> Unit) {
    val seaGreen = Color(0xFF2E8B57)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "noms",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF000000)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
            ) {
                Text("Register", color = Color.White)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Login", color = Color.White)
            }
        }
    }
}

@Composable
fun RegistrationScreen(
    auth: FirebaseAuth, 
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var phoneNumber by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var storedVerificationId by remember { mutableStateOf<String?>(null) }
    val seaGreen = Color(0xFF2E8B57)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "noms",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF000000),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!isCodeSent) {
            PhoneNumberInput(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name", color = seaGreen) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = seaGreen,
                    unfocusedBorderColor = seaGreen
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name", color = seaGreen) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = seaGreen,
                    unfocusedBorderColor = seaGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (phoneNumber.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    coroutineScope.launch {
                        try {
                            val userExists = confirmUser(phoneNumber)
                            if (userExists) {
                                Toast.makeText(context, "User already exists. Please login.", Toast.LENGTH_SHORT).show()
                                onNavigateToLogin()
                            } else {
                                sendVerificationCode(phoneNumber, auth, context) { verificationId ->
                                    storedVerificationId = verificationId
                                    isCodeSent = true
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
            ) {
                Text("Register", color = Color.White)
            }
        } else {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Verification Code", color = seaGreen) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = seaGreen,
                    unfocusedBorderColor = seaGreen
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (verificationCode.isBlank()) {
                        Toast.makeText(context, "Please enter verification code", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    verifyCode(verificationCode, storedVerificationId, auth, context) { success ->
                        if (success) {
                            coroutineScope.launch {
                                try {
                                    createUser(firstName, lastName, phoneNumber)
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                    (context as? ComponentActivity)?.finish()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error creating user: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
            ) {
                Text("Verify Code", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Back", color = Color.White)
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var storedVerificationId by remember { mutableStateOf<String?>(null) }
    val seaGreen = Color(0xFF2E8B57)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "noms",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF000000),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!isCodeSent) {
            PhoneNumberInput(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (phoneNumber.isBlank()) {
                        Toast.makeText(context, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    coroutineScope.launch {
                        try {
                            val userExists = confirmUser(phoneNumber)
                            if (!userExists) {
                                Toast.makeText(context, "User doesn't exist. Please register.", Toast.LENGTH_SHORT).show()
                            } else {
                                sendVerificationCode(phoneNumber, auth, context) { verificationId ->
                                    storedVerificationId = verificationId
                                    isCodeSent = true
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
            ) {
                Text("Login", color = Color.White)
            }
        } else {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Verification Code", color = seaGreen) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = seaGreen,
                    unfocusedBorderColor = seaGreen
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (verificationCode.isBlank()) {
                        Toast.makeText(context, "Please enter verification code", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    verifyCode(verificationCode, storedVerificationId, auth, context) { success ->
                        if (success) {
                            context.startActivity(Intent(context, MainActivity::class.java))
                            (context as? ComponentActivity)?.finish()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
            ) {
                Text("Verify Code", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Back", color = Color.White)
        }
    }
}

private fun sendVerificationCode(
    phoneNumber: String,
    auth: FirebaseAuth,
    context: Context,
    callback: (String) -> Unit
) {
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            callback(verificationId)
        }

        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            TODO("Not yet implemented")
        }
    }

    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(context as ComponentActivity)
        .setCallbacks(callbacks)
        .build()
    PhoneAuthProvider.verifyPhoneNumber(options)
}

private fun verifyCode(
    code: String,
    storedVerificationId: String?,
    auth: FirebaseAuth,
    context: Context,
    onVerificationComplete: (Boolean) -> Unit
) {
    if (storedVerificationId != null) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onVerificationComplete(true)
                } else {
                    Toast.makeText(
                        context,
                        "Verification failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onVerificationComplete(false)
                }
            }
    } else {
        Toast.makeText(context, "Error: No verification ID", Toast.LENGTH_SHORT).show()
        onVerificationComplete(false)
    }
}