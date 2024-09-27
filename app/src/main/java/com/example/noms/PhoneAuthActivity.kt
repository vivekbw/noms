package com.example.noms

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            PhoneAuthScreen(auth)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(auth: FirebaseAuth) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var storedVerificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    val context = LocalContext.current
    val seaGreen = Color(0xFF2E8B57)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isCodeSent) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number", color = seaGreen) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = seaGreen, // Sea green when focused
                    unfocusedBorderColor = seaGreen // Sea green when not focused
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    sendVerificationCode(phoneNumber, auth, context) { verificationId, token ->
                        storedVerificationId = verificationId
                        resendToken = token
                        isCodeSent = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
            ) {
                Text("Send Verification Code", color = Color.White)
            }
        } else {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Verification Code", color = seaGreen) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = seaGreen, // Sea green when focused
                    unfocusedBorderColor = seaGreen // Sea green when not focused
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    verifyCode(verificationCode, storedVerificationId, auth, context)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = seaGreen)
            ) {
                Text("Verify Code", color = Color.White)
            }
        }
    }
}



private fun sendVerificationCode(
    phoneNumber: String,
    auth: FirebaseAuth,
    context: android.content.Context,
    callback: (String?, PhoneAuthProvider.ForceResendingToken?) -> Unit
) {
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential, auth, context)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            callback(verificationId, token)
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

private fun verifyCode(code: String, storedVerificationId: String?, auth: FirebaseAuth, context: android.content.Context) {
    if (storedVerificationId != null) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential, auth, context)
    } else {
        Toast.makeText(context, "Error: No verification ID", Toast.LENGTH_SHORT).show()
    }
}

private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, auth: FirebaseAuth, context: android.content.Context) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as? ComponentActivity)?.finish()
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(context, "Invalid code.", Toast.LENGTH_SHORT).show()
                }
            }
        }
}
