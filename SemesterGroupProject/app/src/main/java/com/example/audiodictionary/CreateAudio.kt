package com.example.audiodictionary

import android.content.pm.PackageManager
import android.media.*
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageMetadata
import java.io.IOException

class CreateAudio : AppCompatActivity(), AudioManager.OnAudioFocusChangeListener {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    private val mAudioPerm = Manifest.permission.RECORD_AUDIO
    private val mFileWritePerm = Manifest.permission.WRITE_EXTERNAL_STORAGE

    private lateinit var startRecordingBtn: ImageView
    private lateinit var playRecordingBtn: Button
    private lateinit var submitBtn: Button
    private lateinit var ringView: ImageView

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
    private lateinit var username : String
    private lateinit var wordID : String

    private lateinit var mDatabaseRecordings : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_audio)

        uid = intent.getStringExtra("USER_ID").toString()
        username = intent.getStringExtra("USERNAME").toString()
        wordID = intent.getStringExtra("WORD_ID").toString()

        mDatabaseRecordings = FirebaseDatabase.getInstance().getReference("RecordingList").child(wordID)

        fileName = application.getExternalFilesDir(null)?.absolutePath + "/" + System.currentTimeMillis() + ".3gp"

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mStorage = FirebaseStorage.getInstance().reference

        startRecordingBtn = findViewById(R.id.imageView_mic)
        playRecordingBtn = findViewById(R.id.button5)
        submitBtn = findViewById(R.id.button4)
        ringView = findViewById(R.id.imageView_ring)

        ringView.visibility = View.GONE

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
                            ringView.visibility = View.VISIBLE
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
                    ringView.visibility = View.GONE
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

    // Uploads the recording to Firebase Storage as well as calls function to add path of
    // stored file to Firebase Database
    private fun upload() {
        val uri = Uri.fromFile(File(fileName))
        val fileRef = mStorage.child(wordID + "/" + uri.lastPathSegment!!)

        val metadata = StorageMetadata.Builder().setContentType("audio/3gp").build()

       try {
           fileRef.putFile(uri, metadata).continueWithTask { uploadTask ->
               if(!uploadTask.isSuccessful) {
                   throw uploadTask.exception!!
               }

               return@continueWithTask fileRef.downloadUrl
           }.addOnCompleteListener { task ->
               if (task.isSuccessful) {
                   Toast.makeText(applicationContext, "Recording Uploaded", Toast.LENGTH_LONG).show()
               }
           }

           addToDatabase(uri)

       } catch (e : Exception) {
           Log.e("CreateAudio", e.toString())
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

    // Saves the path to the audio file to the database for future use
    private fun addToDatabase(uri: Uri) {
        val id = mDatabaseRecordings.child(wordID).push().key
        val record = Recording(wordID + "/" + uri.lastPathSegment.toString(), username, uid)

        if (id != null) {
            mDatabaseRecordings.child(id).setValue(record)
        }
    }


    companion object {
        private const val REQUEST_CODE = 1
        private const val TAG = "CreateAudio"
    }

}