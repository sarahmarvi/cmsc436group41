package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LoginActivity : AppCompatActivity() {

    private lateinit var createAccountButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        createAccountButton = findViewById(R.id.createAccBtn)

        createAccountButton.setOnClickListener {
            startCreateAcc()
        }

    }

    private fun startCreateAcc() {
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }
}