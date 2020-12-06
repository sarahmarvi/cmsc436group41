package com.example.audiodictionary

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.gesture.GestureLibraries.fromFile
import android.media.*
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
import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import java.io.IOException

class CreateAudio : Activity(), AudioManager.OnAudioFocusChangeListener {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    private val mAudioPerm = Manifest.permission.RECORD_AUDIO
    private val mFileWritePerm = Manifest.permission.WRITE_EXTERNAL_STORAGE

    private var audioFilePath: String? = null

    private lateinit var startRecordingBtn: Button
    private lateinit var playRecordingBtn: Button
    private lateinit var submitBtn: Button

    private var mCanRecord : Boolean = false
    private var mHasStartedR: Boolean = false

    private var mHasOneRecording: Boolean = false
    private var mHasStartedPlay: Boolean = false

    private lateinit var mAudioAttributes: AudioAttributes
    private lateinit var mAudioFocusRequest: AudioFocusRequest
    private lateinit var mAudioManager: AudioManager
    private val mFocusLock = Any()
    private var playDelay : Boolean = false
    private var focus : Boolean = false

    private lateinit var mStorage: StorageReference
    private lateinit var fileName: String
    private lateinit var uid: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_audio)

        fileName = application.getExternalFilesDir(null)?.absolutePath + "/recording123.3gp"

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mStorage = FirebaseStorage.getInstance().getReference()

        startRecordingBtn = findViewById(R.id.button3)
        playRecordingBtn = findViewById(R.id.button4)
        submitBtn = findViewById(R.id.button5)

        // Check for the audio and write permissions
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(mAudioPerm) &&
            PackageManager.PERMISSION_GRANTED == checkSelfPermission(mFileWritePerm)) {
            // the recording can proceed
            mCanRecord = true
        } else {
            // cannot proceed
            requestPermissions(arrayOf(mAudioPerm,mFileWritePerm), REQUEST_CODE)
        }

        mAudioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()

        mAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(mAudioAttributes).setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this, Handler(Looper.getMainLooper()))
            .setOnAudioFocusChangeListener(this, Handler(Looper.getMainLooper()))
            .build()




        startRecordingBtn.setOnClickListener {
            if (mCanRecord) {
                if (!mHasStartedR) { // Start recording
                    mHasStartedR = true
                    mediaRecorder = MediaRecorder()
                    mediaRecorder.apply {
                        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        mediaRecorder?.setOutputFile(fileName)
                        try {
                            mediaRecorder?.prepare()
                            Toast.makeText(applicationContext, "Recording Started", Toast.LENGTH_LONG).show()
                            mediaRecorder?.start()
                        } catch (exception : IOException) {
                            Log.i(TAG,"Failed to prepare and start recording")
                        }
                    }
                } else { // Stop recording
                    Toast.makeText(applicationContext, "Recording Stopped", Toast.LENGTH_LONG).show()
                    mHasStartedR = false
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                    mediaRecorder = null
                    mHasOneRecording = true
                }

            }
        }

        playRecordingBtn.setOnClickListener {
            if (!mHasStartedR && mHasOneRecording && !mHasStartedPlay) { //Start Playing
                mediaPlayer = MediaPlayer()
                mediaPlayer?.apply {
                    mediaPlayer?.setAudioAttributes(mAudioAttributes)
                    mediaPlayer?.setOnCompletionListener {
                        mHasStartedPlay = true
                    }
                    try {
                        if (File(fileName).exists()) {
                            mediaPlayer?.apply {
                                mediaPlayer?.setDataSource(fileName)
                                mediaPlayer?.prepare()
                                audioFocusRequest()
                            }
                        } else {
                            mHasStartedPlay = false
                        }
                    } catch (exception : IOException) {
                        Log.i(TAG,"Failed")
                    }
                    mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest)
                }

            } else if (!mHasStartedR && mHasStartedPlay) { // Stop Playing
                mediaPlayer?.apply {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer?.stop()
                        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest)
                    }
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
                mHasStartedPlay = false
            }
        }



//        audioFilePath = Environment.getExternalStorageDirectory()
//            .absolutePath + "/myaudio.3gp"

        submitBtn.setOnClickListener { upload() }
    }



    private fun audioFocusRequest() {
        val res = mAudioManager.requestAudioFocus(mAudioFocusRequest)
        synchronized(mFocusLock) {
            when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    playDelay = false
                    mediaPlayer?.start()
                }
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> playDelay = true
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> playDelay = false
                else -> Log.i(TAG, "Failed in audioFocusRequest")
            }
        }

    }


    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> if (focus || playDelay) {
                synchronized(mFocusLock) {
                    playDelay = false
                    focus = false
                }
                mediaPlayer?.start()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(mFocusLock) {
                    focus = mediaPlayer!!.isPlaying
                    playDelay = false
                }
                mediaPlayer?.apply {
                    if (mediaPlayer!!.isPlaying)
                        pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(mFocusLock) {
                    focus = false
                    playDelay = false
                }
                mediaPlayer?.apply {
                    if (mediaPlayer!!.isPlaying)
                        pause()
                }
            }
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, results: IntArray) {
        onRequestPermissionsResult(requestCode, permissions, results)
        if (REQUEST_CODE == requestCode) {
            for (r in results) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "Permission" + r + "not granted", Toast.LENGTH_LONG).show()
                    break
                }
            }
        }
    }


    companion object {
        private const val REQUEST_CODE = 1
        private const val TAG = "CreateAudio"
    }

}