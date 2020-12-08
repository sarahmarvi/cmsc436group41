package com.example.audiodictionary

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.lang.Exception

// This class is to populate the layout learner_word_details, in which a learner is to see a word
// with all the audios and ratings listed below.

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

        langCode = intent.getStringExtra(LANGUAGE_KEY).toString()
        wordId = intent.getStringExtra(WORD_ID_KEY).toString()
        wdOriginal = intent.getStringExtra(ORIGINAL_KEY).toString()
        wdTranslation = intent.getStringExtra(TRANSLATION_KEY).toString()

        mDatabaseRecordings = FirebaseDatabase.getInstance().getReference(RECORDING_LIST_TEXT)

        mTitle = findViewById(R.id.learner_word_title)
        mTitle.text = "$wdOriginal - $wdTranslation"

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
                        Log.e(TAG, e.toString())
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

    companion object {
        const val TAG = "LearnerWordActivity"
        const val LANGUAGE_KEY = "LANGUAGE"
        const val WORD_ID_KEY = "WORD_ID"
        const val ORIGINAL_KEY = "ORIGINAL"
        const val TRANSLATION_KEY = "TRANSLATION"
        const val RECORDING_LIST_TEXT = "RecordingList"
    }



}