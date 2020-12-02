package com.example.audiodictionary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LanguageListActivity : AppCompatActivity() {

//    private val prevIntent = getIntent()
//    private lateinit var uid : String

    private lateinit var arabicButton: Button
    private lateinit var frenchButton: Button
    private lateinit var italianButton: Button
    private lateinit var spanishButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.languages)

        
//        if (prevIntent.hasExtra("UserId")) {
//            uid = prevIntent.getStringExtra("UserID").toString()
//        }


        arabicButton = findViewById(R.id.arabic_open)
        frenchButton = findViewById(R.id.french_open)
        italianButton = findViewById(R.id.italian_open)
        spanishButton = findViewById(R.id.spanish_open)

    }
}