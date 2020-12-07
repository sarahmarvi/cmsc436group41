package com.example.audiodictionary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.lang.Exception

class LearnerWordActivity : AppCompatActivity() {

    internal lateinit var mListViewRecordings: ListView
    private lateinit var mTitle: TextView

    private lateinit var mDatabaseRecordings : DatabaseReference
    private lateinit var mRatingsSnapshot: DataSnapshot

    private lateinit var langCode : String
    private lateinit var wordId : String
    private lateinit var wdOriginal : String
    private lateinit var wdTranslation : String

    private lateinit var recordings : MutableList<Recording>
    private lateinit var recordingIds : MutableList<String>
    private lateinit var ratings : MutableList<Ratings>
    private lateinit var uids : MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learner_word_details)

        recordings = ArrayList()
        recordingIds = ArrayList()
        ratings = ArrayList()
        uids = ArrayList()

        langCode = intent.getStringExtra("LANGUAGE").toString()
        wordId = intent.getStringExtra("WORD_ID").toString()
        wdOriginal = intent.getStringExtra("ORIGINAL").toString()
        wdTranslation = intent.getStringExtra("TRANSLATION").toString()

        mDatabaseRecordings = FirebaseDatabase.getInstance().getReference("RecordingList")

        mTitle = findViewById(R.id.learner_word_title)
        mTitle.text = wdTranslation

        mListViewRecordings = findViewById(R.id.learner_record_rate_list)

    }

    // Adapted from Lab7-Firebase
    override fun onStart() {
        super.onStart()

        mDatabaseRecordings.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                recordings.clear()
                mRatingsSnapshot = dataSnapshot

                var record : Recording? = null
                for (postSnapshot in dataSnapshot.child(wordId).children) {
                    try {
                        record = postSnapshot.getValue(Recording::class.java)
                        postSnapshot.key?.let { recordingIds.add(it) }
                    } catch (e: Exception) {
                        Log.e("LearnerLanguage", e.toString())
                    } finally {
                        recordings.add(record!!)
                    }
                }
        val recordingListAdapter = LearnerRecordingList(
            this@LearnerWordActivity,
            recordings,
            recordingIds,
            mRatingsSnapshot
        )
        mListViewRecordings.adapter = recordingListAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }

    // To allow a user to exit/sign out without letting them use the original function of the back
    // button to go back (to a page exclusive to users signed in) after signing out.
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}