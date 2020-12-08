package com.example.audiodictionary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.lang.Exception

// This class is to populate the layout native_word_details, in which a native is to see a word
// with all the audios and ratings listed below, and that they can add ratings to.

class NativeWordActivity : AppCompatActivity() {

    internal lateinit var mListViewRecordings: ListView
    private var mTitle: TextView? = null
    private lateinit var mRecordBtn : Button

    private lateinit var mDatabaseRecordings : DatabaseReference
    private lateinit var mRatingsSnapshot: DataSnapshot

    private lateinit var langCode : String
    private lateinit var wordId : String
    private lateinit var wdOriginal : String
    private lateinit var wdTranslation : String
    private lateinit var user : String
    private lateinit var uid : String

    private lateinit var recordings : MutableList<Recording>
    private lateinit var recordingIds : MutableList<String>
    private lateinit var ratings : MutableList<Ratings>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.native_word_details)

        recordings = ArrayList()
        recordingIds = ArrayList()
        ratings = ArrayList()

        user = intent.getStringExtra(USERNAME_KEY)!!
        uid = intent.getStringExtra(USER_ID_KEY)!!
        langCode = intent.getStringExtra(LANGUAGE_KEY)!!
        wordId = intent.getStringExtra(WORD_ID_KEY)!!
        wdOriginal = intent.getStringExtra(ORIGINAL_KEY)!!
        wdTranslation = intent.getStringExtra(TRANSLATION_KEY)!!

        mDatabaseRecordings = FirebaseDatabase.getInstance().getReference(RECORDING_LIST_TEXT)


        mTitle = findViewById(R.id.native_word_title)
        mTitle!!.text = "$wdOriginal - $wdTranslation"

        mListViewRecordings = findViewById(R.id.native_record_rate_list)

        mRecordBtn = findViewById(R.id.addAudioButton)

        mRecordBtn.setOnClickListener {

            val clickIntent = Intent(this@NativeWordActivity, CreateAudio::class.java)

            clickIntent.putExtra(WORD_ID_KEY, wordId)
            clickIntent.putExtra(USERNAME_KEY, user)
            clickIntent.putExtra(USER_ID_KEY, uid)

            startActivity(clickIntent)
        }

    }

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
                val recordingListAdapter = NativeRecordingList(this@NativeWordActivity, recordings, recordingIds, uid, mRatingsSnapshot)
                mListViewRecordings.adapter = recordingListAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }

    companion object {
        const val TAG = "NativeWordActivity"
        const val USERNAME_KEY = "USERNAME"
        const val USER_ID_KEY = "USER_ID"
        const val LANGUAGE_KEY = "LANGUAGE"
        const val WORD_ID_KEY = "WORD_ID"
        const val ORIGINAL_KEY = "ORIGINAL"
        const val TRANSLATION_KEY = "TRANSLATION"
        const val RECORDING_LIST_TEXT = "RecordingList"
    }


}