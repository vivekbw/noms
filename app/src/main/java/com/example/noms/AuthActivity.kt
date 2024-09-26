package com.example.noms

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        val btnGetStarted: Button = findViewById(R.id.btnGetStarted)
        val tvLogin: TextView = findViewById(R.id.tvLogin)

        btnGetStarted.setOnClickListener {
            startPhoneAuthentication()
        }

        tvLogin.setOnClickListener {
            startPhoneAuthentication()
        }
    }

    private fun startPhoneAuthentication() {
        val intent = Intent(this, PhoneAuthActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, start MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
