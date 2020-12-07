package com.example.audiodictionary

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

    private lateinit var mDatabaseLanguage : DatabaseReference
    private lateinit var mDatabaseRecordings : DatabaseReference
    private lateinit var mDatabaseRatings : DatabaseReference

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

        mDatabaseLanguage = FirebaseDatabase.getInstance().getReference("Languages").child(langCode)
        mDatabaseRecordings = FirebaseDatabase.getInstance().getReference("RecordingList").child(wordId)
        mDatabaseRatings = FirebaseDatabase.getInstance().getReference("Ratings")


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

                var record : Recording? = null
                for (postSnapshot in dataSnapshot.children) {
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
            uids
        )
        mListViewRecordings.adapter = recordingListAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }

}