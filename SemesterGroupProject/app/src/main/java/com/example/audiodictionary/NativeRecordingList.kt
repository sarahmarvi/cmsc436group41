package com.example.audiodictionary

import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

// Adapted from Lab7-Firebase for populating the list view of NativeWordActivity
class NativeRecordingList(
    private val context: Activity,
    private var recordings: List<Recording>,
    private var recordingID: List<String>,
    private val uid: String,
    private val mRatingsSnapshot: DataSnapshot
) : ArrayAdapter<Recording>(context,
    R.layout.audio_list, recordings) {

    private lateinit var ratingBar : RatingBar

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.audio_list, null, true)

        val textViewUserName = listViewItem.findViewById<View>(R.id.placeForNameTextView) as TextView
        val playBtn = listViewItem.findViewById<Button>(R.id.audioListPlayButton)
        ratingBar = listViewItem.findViewById(R.id.ratingBar)

        val record = recordings[position]
        textViewUserName.text = record.user
        getRating(recordingID[position], mRatingsSnapshot, uid)

        playBtn.setOnClickListener { playAudio(recordings[position]) }
        ratingBar.setOnRatingBarChangeListener { _: RatingBar?, rating: Float, _: Boolean ->
            Log.i(TAG, "Just received a rating of $rating")
            sendRating(recordingID[position], uid, rating)
        }

        return listViewItem
    }

    private fun playAudio(record : Recording) {
        val storage = FirebaseStorage.getInstance()

        storage.reference.child(record.audioFile).downloadUrl.addOnSuccessListener {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(it.toString())
            mediaPlayer.setOnPreparedListener { player ->
                player.start()
            }
            mediaPlayer.prepareAsync()
        }
    }

    // Stores the user's rating for the recording in the database. Everytime the user changes their
    // rating it gets updated here as well
    private fun sendRating(recordingID: String, uid: String, ratings: Float) {
        val rating = Ratings(ratings)
        val mDatabaseRatings = FirebaseDatabase.getInstance().getReference("RecordingList")

        mDatabaseRatings.child("Ratings").child(recordingID).child(uid).setValue(rating)
        Log.i(TAG, "User has given <Recording: $recordingID> a $rating")
    }

    // Gets the most current rating of the user for the selected recording
    private fun getRating (recordingID : String, snapshot: DataSnapshot, uid : String) {
        val rating = snapshot.child("Ratings").child(recordingID).child(uid).getValue(Ratings::class.java)

        if (rating != null) {
            ratingBar.rating = rating.rating
        }
    }

    companion object {
        const val TAG = "RecordingList"
    }
}