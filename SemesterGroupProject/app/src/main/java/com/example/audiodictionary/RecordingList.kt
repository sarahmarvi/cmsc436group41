package com.example.audiodictionary

import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception

class RecordingList (private val context: Activity, private var recordings: List<Recording>) : ArrayAdapter<Recording>(context,
    R.layout.audio_list, recordings) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.audio_list, null, true)

        val textViewUserName = listViewItem.findViewById<View>(R.id.textView9) as TextView
        val playBtn = listViewItem.findViewById<Button>(R.id.button2)

        val record = recordings[position]
        textViewUserName.text = record.user

        playBtn.setOnClickListener { playAudio(recordings[position]) }

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
}