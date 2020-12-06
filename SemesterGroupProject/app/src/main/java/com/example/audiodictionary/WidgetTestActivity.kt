package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView

class WidgetTestActivity : AppCompatActivity() {

//    private val prevIntent = getIntent()
//    private lateinit var uid : String

    private lateinit var greetingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_test)

        //val theIntent = getIntent() as Intent
        //val user = theIntent.getStringExtra("theUser").toString()

        //greetingTextView = findViewById(R.id.languages_greeting)
        //greetingTextView.setText("Welcome, " + user)
        // TODO: move this to the real recording list
        val ratingBar = findViewById<View>(R.id.test_rating_bar) as RatingBar
        //ratingBar.isEnabled = false // this is how we disable it for learners
        //ratingBar.rating = 3.5f // this is how we set a rating I believe
        ratingBar.setIsIndicator(false) // actually this is how we disable it
        ratingBar.setOnRatingBarChangeListener {
                _: RatingBar?, rating: Float, fromUser: Boolean ->
            // you should have a local variable letting you know what recording this actually is
            // use that recording ID to send the rating to the DB using the structure outlined in our doc
            // check for an existing rating by the user, and if so, replace it; otherwise, make a new one
            Log.i(TAG, "Just received a rating of $rating")
            true
        }

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

    companion object {
        const val TAG = "AudioDictionary"
    }
}