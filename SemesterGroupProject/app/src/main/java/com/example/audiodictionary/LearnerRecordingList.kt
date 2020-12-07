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
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class LearnerRecordingList(
    private val context: Activity,
    private var recordings: List<Recording>,
    private var recordingID: List<String>,
    private var uids: List<String>
) : ArrayAdapter<Recording>(context,
    R.layout.audio_list, recordings) {

    private lateinit var ratingBar : RatingBar

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.audio_list, null, true)

        val textViewUserName = listViewItem.findViewById<View>(R.id.textView9) as TextView
        val playBtn = listViewItem.findViewById<Button>(R.id.button2)
        ratingBar = listViewItem.findViewById(R.id.ratingBar)

        val record = recordings[position]
        textViewUserName.text = record.user

        getRating(recordingID[position])

        playBtn.setOnClickListener { playAudio(recordings[position]) }
        ratingBar.setOnRatingBarChangeListener { _: RatingBar?, rating: Float, _: Boolean ->
            Log.i(NativeRecordingList.TAG, "Just received a rating of $rating")
        }

        return listViewItem
    }

    private fun playAudio(record : Recording) {
        val storage = FirebaseStorage.getInstance()

        val storageRef = storage.reference.child(record.audioFile).downloadUrl.addOnSuccessListener {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(it.toString())
            mediaPlayer.setOnPreparedListener { player ->
                player.start()
            }
            mediaPlayer.prepareAsync()
        }
    }

    private fun getRating(recordingID: String) {

        if (uids.isEmpty()) {
            return
        }

        val mDatabaseRatings = FirebaseDatabase.getInstance().getReference("Ratings").child(recordingID)

        mDatabaseRatings.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                var ratingNum = 0f
                var counter = 0f

                var user : Ratings?
                for (postSnapshot in dataSnapshot.children) {
                    user = postSnapshot.getValue(Ratings::class.java)

                    ratingNum += user!!.rating
                    counter += 1f

                }

                ratingBar.rating = (ratingNum/counter)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })

    }
}