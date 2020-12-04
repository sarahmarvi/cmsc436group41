package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class LanguageListActivity : AppCompatActivity() {

//    private val prevIntent = getIntent()
//    private lateinit var uid : String

//    private lateinit var arabicButton: Button
//    private lateinit var frenchButton: Button
//    private lateinit var italianButton: Button
//    private lateinit var spanishButton: Button
    private lateinit var greetingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.languages)

        val theIntent = getIntent() as Intent
        val user = theIntent.getStringExtra("theUser").toString()

        greetingTextView = findViewById(R.id.languages_greeting)
        greetingTextView.setText("Welcome, " + user)

//        if (prevIntent.hasExtra("UserId")) {
//            uid = prevIntent.getStringExtra("UserID").toString()
//        }


//        arabicButton = findViewById(R.id.arabic_open)
//        frenchButton = findViewById(R.id.french_open)
//        italianButton = findViewById(R.id.italian_open)
//        spanishButton = findViewById(R.id.spanish_open)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        return true
    }

}