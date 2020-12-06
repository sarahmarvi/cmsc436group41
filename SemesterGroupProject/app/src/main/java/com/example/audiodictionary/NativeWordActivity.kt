package com.example.audiodictionary

import android.content.Intent
import android.media.MediaPlayer
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

    private lateinit var mDatabaseLanguage : DatabaseReference
    //    private lateinit var mDatabaseWord : DatabaseReference
    private lateinit var mDatabaseRecordings : DatabaseReference
    private lateinit var mDatabaseRatings : DatabaseReference

    private lateinit var langCode : String
    private lateinit var wordId : String
    private lateinit var wdOriginal : String
    private lateinit var wdTranslation : String

    private lateinit var recordings : MutableList<Recording>
    private lateinit var recordingIds : MutableList<String>
    private lateinit var ratings : MutableList<Ratings>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learner_word_details)

        recordings = ArrayList()
        recordingIds = ArrayList()
        ratings = ArrayList()

        val intent = getIntent() as Intent
        langCode = intent.getStringExtra("LANGUAGE").toString()
        wordId = intent.getStringExtra("WORD_ID").toString()
        wdOriginal = intent.getStringExtra("ORIGINAL").toString()
        wdTranslation = intent.getStringExtra("TRANSLATION").toString()

        mDatabaseLanguage = FirebaseDatabase.getInstance().getReference("Languages").child(langCode)
//        mDatabaseWord = FirebaseDatabase.getInstance().getReference("Words").child(langCode)
        mDatabaseRecordings = FirebaseDatabase.getInstance().getReference("RecordingList").child(wordId)
        mDatabaseRatings = FirebaseDatabase.getInstance().getReference("Ratings")


        mTitle = findViewById(R.id.learner_word_title)
        mTitle!!.text = wdTranslation

        mListViewRecordings = findViewById(R.id.learner_record_rate_list)

        mRecordBtn = findViewById(R.id.add_audio_button)

        mRecordBtn.setOnClickListener {

            val clickIntent = Intent(this@NativeWordActivity, CreateAudio::class.java)

            startActivity(clickIntent)
        }

    }

    override fun onStart() {
        super.onStart()

        mDatabaseRecordings.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                recordings.clear()

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
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })

        // TODO - Get Ratings and add them to adapter
        val recordingListAdapter = RecordingList(this@NativeWordActivity, recordings)
        mListViewRecordings.adapter = recordingListAdapter
    }

//    private fun playAudio() {
//        val player = MediaPlayer()
//
//        // TODO - Figure out how to get URL from Firebase
//        player.setDataSource("")
//
//        try {
//            player.setOnPreparedListener(MediaPlayer.OnPreparedListener {
//
//                override fun onPrepared(mp : MediaPlayer) {
//                    mp.start()
//                }
//            })
//            player.prepare()
//        } catch (e: Exception) {
//            Log.e("NativeWordActivity", e.toString())
//        }
//
//    }
}