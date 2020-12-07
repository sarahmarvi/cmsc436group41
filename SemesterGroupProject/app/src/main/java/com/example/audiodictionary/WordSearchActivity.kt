package com.example.audiodictionary

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class WordSearchActivity : AppCompatActivity() {
    private lateinit var mDatabaseWords : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "Creating WordSearchActivity!")
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.widget_test)

        //mDatabaseWords = FirebaseDatabase.getInstance().getReference("Words")

        // handle search intents
        onSearchIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        onSearchIntent(intent)
    }

    private fun onSearchIntent(intent: Intent) {
        Log.i(TAG, "Just got a new search intent!")
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                //doMySearch(query)
                searchWord(query, "fr") // for testing I've hardcoded this
            }
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
    /*fun sendRating(recordingID: String, uid: String, username: String, rating: Float) {
        val rating = Rating(username, rating)
        // if the user has an existing rating, this will replace it
        // thanks to the way the tree is structured
        mDatabaseRatings.child(recordingID).child(uid).setValue(rating)
        Log.i(TAG, "User $username has given <Recording: $recordingID> a $rating")
    }*/

    // This searches using the word and the 2-letter language key
    private fun searchWord(word: String, language: String) {
        //val words = mDatabaseWords.child(language)
        //Log.i(TAG, "The words retrieved from $language: $words")
        Log.i(TAG, "The words retrieved from $language")
    }

    companion object {
        const val TAG = "WordSearchActivity"
    }
}