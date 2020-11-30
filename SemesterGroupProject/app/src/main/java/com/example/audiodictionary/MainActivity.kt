package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var learnerButton: Button
    private lateinit var nativeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome)

        learnerButton = findViewById(R.id.learnerBtn)
        nativeButton = findViewById(R.id.nativeBtn)

        learnerButton.setOnClickListener {
            startLearner()
        }
        nativeButton.setOnClickListener {
            startNative()
        }
    }

    private fun startLearner() {
        val intent = Intent(this, LanguageListActivity::class.java)
        startActivity(intent)
    }

    private fun startNative() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}