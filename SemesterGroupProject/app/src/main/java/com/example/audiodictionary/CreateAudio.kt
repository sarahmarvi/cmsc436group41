package com.example.audiodictionary

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.gesture.GestureLibraries.fromFile
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File

class CreateAudio : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    private var audioFilePath: String? = null
    private var isRecording = false

    private lateinit var startRecordingBtn: Button
    private lateinit var playRecordingBtn: Button
    private lateinit var submitBtn: Button

    private lateinit var mStorage: StorageReference
    private lateinit var fileName: String
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_audio)

        mStorage = FirebaseStorage.getInstance().getReference()

        startRecordingBtn = findViewById(R.id.button3)
        playRecordingBtn = findViewById(R.id.button4)
        submitBtn = findViewById(R.id.button5)

        submitBtn.setOnClickListener { upload() }
    }

    private fun upload() {

        val filepath = mStorage.child("Audio").child(uid)
        val uri = Uri.fromFile(File(audioFilePath))

        try {
            filepath.putFile(uri).addOnSuccessListener {
                Toast.makeText(this, "Successfully Uploaded", Toast.LENGTH_LONG).show()
            }
        }catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }


    }

}