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
import com.google.firebase.storage.FirebaseStorage

// Adapted from Lab7-Firebase to populate the list view in LearnerWordActivity

// This class is for populating the component of the audio list under a particular word that has
// been selected. The component includes the user who uploaded the audio, the ratings of the audio,
// and the audio to be played itself.

class LearnerRecordingList(
    private val context: Activity,
    private var recordings: List<Recording>,
    private var recordingID: List<String>,
    private val mRatingsSnapshot: DataSnapshot
) : ArrayAdapter<Recording>(context,
    R.layout.audio_list, recordings) {

    private lateinit var ratingBar : RatingBar
    private lateinit var textViewUserName : TextView

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.audio_list, null, true)

        textViewUserName = listViewItem.findViewById<View>(R.id.placeForNameTextView) as TextView
        val playBtn = listViewItem.findViewById<Button>(R.id.audioListPlayButton)
        ratingBar = listViewItem.findViewById(R.id.ratingBar)

        val record = recordings[position]
        textViewUserName.text = record.user

        getRating(recordingID[position], mRatingsSnapshot)
        ratingBar.isEnabled = false

        playBtn.setOnClickListener { playAudio(recordings[position]) }
        ratingBar.setOnRatingBarChangeListener { _: RatingBar?, rating: Float, _: Boolean ->
            Log.i(NativeRecordingList.TAG, "Just received a rating of $rating")
        }

        return listViewItem
    }

    // For playing the audio from firebase
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

    // For getting the rating from firebase to be put into the respective audio sample in the list
    private fun getRating(recordingID: String, snapshot: DataSnapshot) {
        var ratingNum = 0f
        var counter = 0f
        var user : Ratings?
        val snapshotChildren = snapshot.child(RATINGS_TEXT).child(recordingID).children

        for (postSnapshot in snapshotChildren) {
            user = postSnapshot.getValue(Ratings::class.java)

            ratingNum += user!!.rating
            counter += 1f
        }

        if (counter == 0f) {
            ratingBar.rating = 0f
        }

        ratingBar.rating = (ratingNum/counter)
        Log.i(TAG, "Counter = $counter Ratingnum = $ratingNum")
    }

    companion object {
        const val TAG = "LearnerRecordingList"
        const val RATINGS_TEXT = "Ratings"
    }

}