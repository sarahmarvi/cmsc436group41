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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class WidgetTestActivity : AppCompatActivity() {
    private lateinit var mDatabaseRatings : DatabaseReference
    private lateinit var mDatabaseWords : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_test)

        mDatabaseRatings = FirebaseDatabase.getInstance().getReference("Ratings")
        mDatabaseWords = FirebaseDatabase.getInstance().getReference("Words")

        // TODO: move this to the real recording list
        val ratingBar = findViewById<View>(R.id.test_rating_bar) as RatingBar
        ratingBar.rating = 3.5f // this is how we set a rating
        //ratingBar.setIsIndicator(false) // this is how we disable it for learners
        ratingBar.setOnRatingBarChangeListener { _: RatingBar?, rating: Float, _: Boolean ->
            // you should have a local variable letting you know what recording this actually is
            // use that recording ID to send the rating to the DB using the structure outlined in our doc
            // check for an existing rating by the user, and if so, replace it; otherwise, make a new one
            Log.i(TAG, "Just received a rating of $rating")
            // uncomment this with the values below
            //sendRating(recordingID, uid, username, rating)
            true
        }
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

    // We need to search by recording ID and user ID, so those are the keys
    // We also need the rating from the listener above, and we need the username for the view
    fun sendRating(recordingID: String, uid: String, username: String, rating: Float) {
        val rating = Rating(username, rating)
        // if the user has an existing rating, this will replace it
        // thanks to the way the tree is structured
        mDatabaseRatings.child(recordingID).child(uid).setValue(rating)
        Log.i(TAG, "User $username has given <Recording: $recordingID> a $rating")
    }

    companion object {
        const val TAG = "WidgetTestActivity"
    }
}