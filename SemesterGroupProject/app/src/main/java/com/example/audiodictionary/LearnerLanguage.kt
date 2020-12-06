package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import com.google.firebase.database.*
import java.lang.Exception

class LearnerLanguage : AppCompatActivity() {

    internal lateinit var listViewWords: ListView
    internal lateinit var words : MutableList<Word>

    private lateinit var databaseLanguage : DatabaseReference
    private lateinit var databaseWords : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learner_language)

        words = ArrayList()

        databaseWords =  FirebaseDatabase.getInstance().getReference("Languages").child(
            intent.getStringExtra("LANGUAGE").toString()).child("words")

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

    // Adapted from Lab7-Firebase
    override fun onStart() {
        super.onStart()

        databaseWords.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                words.clear()

                var word : Word? = null
                for (postSnapshot in dataSnapshot.children) {
                    try {
                        word = postSnapshot.getValue(Word::class.java)
                    } catch (e: Exception) {
                        Log.e("LearnerLanguage", e.toString())
                    } finally {
                        words.add(word!!)
                    }
                }
                val wordListAdapter = WordList(this@LearnerLanguage, words)
                listViewWords.adapter = wordListAdapter

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }
}