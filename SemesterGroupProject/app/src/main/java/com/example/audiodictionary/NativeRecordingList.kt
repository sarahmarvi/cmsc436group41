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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class NativeRecordingList(
    private val context: Activity,
    private var recordings: List<Recording>,
    private var recordingID: List<String>,
    private val uid: String
) : ArrayAdapter<Recording>(context,
    R.layout.audio_list, recordings) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.audio_list, null, true)

        val textViewUserName = listViewItem.findViewById<View>(R.id.textView9) as TextView
        val playBtn = listViewItem.findViewById<Button>(R.id.button2)
        val ratingBar = listViewItem.findViewById<RatingBar>(R.id.ratingBar)

        val record = recordings[position]
        textViewUserName.text = record.user

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

    private fun sendRating(recordingID: String, uid: String, ratings: Float) {
        val rating = Ratings(ratings)
        val mDatabaseRatings = FirebaseDatabase.getInstance().getReference("Ratings")

        mDatabaseRatings.child(recordingID).child(uid).setValue(rating)
        Log.i(TAG, "User has given <Recording: $recordingID> a $rating")
    }

    companion object {
        const val TAG = "RecordingList"
    }
}