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

        user = intent.getStringExtra("USERNAME")!!
        uid = intent.getStringExtra("USER_ID")!!
        langCode = intent.getStringExtra("LANGUAGE")!!
        wordId = intent.getStringExtra("WORD_ID")!!
        wdOriginal = intent.getStringExtra("ORIGINAL")!!
        wdTranslation = intent.getStringExtra("TRANSLATION")!!

        mDatabaseRecordings = FirebaseDatabase.getInstance().getReference("RecordingList")


        mTitle = findViewById(R.id.native_word_title)
        mTitle!!.text = wdOriginal

        mListViewRecordings = findViewById(R.id.native_record_rate_list)

        mRecordBtn = findViewById(R.id.add_audio_button)

        mRecordBtn.setOnClickListener {

            val clickIntent = Intent(this@NativeWordActivity, CreateAudio::class.java)

            clickIntent.putExtra("WORD_ID", wordId)
            clickIntent.putExtra("USERNAME", user)
            clickIntent.putExtra("USER_ID", uid)

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
                        Log.e("LearnerLanguage", e.toString())
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
}