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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageMetadata
import java.io.IOException

// To write this class, there was reliance on developer.android.com and Dr. Porter's class example
// of AudioVideoAudioRecording

class CreateAudio : AppCompatActivity(), AudioManager.OnAudioFocusChangeListener {


    // The mediaRecorder allows the app to record audio from native speakers
    private var mediaRecorder: MediaRecorder? = null
    // The mediaPlayer allows the app to play audio after recording
    private var mediaPlayer: MediaPlayer? = null

    // The permissions needed to record audio and write the audio file to the sd card folder.
    private val mAudioPerm = Manifest.permission.RECORD_AUDIO
    private val mFileWritePerm = Manifest.permission.WRITE_EXTERNAL_STORAGE

    // startRecordingBtn is the orange microphone image used to start and stop the audio recording
    private lateinit var startRecordingBtn: ImageView
    // used to play back the audio once at least one has been recorded
    private lateinit var playRecordingBtn: Button
    // used to submit the locally recorded audio to firebase
    private lateinit var submitBtn: Button
    // a blue ring that surrounds the orange microphone to when the app is recording
    private lateinit var ringView: ImageView

    // mCanRecord is to make sure the permissions have been granted before attempting to record
    private var mCanRecord : Boolean = false
    // mHasStartedR is to indicate that recording has started. This is used to know what functionality
    // the click on the microphone will have, whether it be start or stop recording.
    private var mHasStartedR: Boolean = false

    // mHasOneRecording is to indicate whether at least one recording has been made, because the app
    // cannot play a recording without at least one existing.
    private var mHasOneRecording: Boolean = false
    // mHasStartedPlay is to indicate the audio file has started playing, used to alternate functionality
    // between start and stop playing
    private var mHasStartedPlay: Boolean = false

    // mAudioAttributes is used to determine how the audio file is going to be played
    private lateinit var mAudioAttributes: AudioAttributes
    // mAudioFocusRequest is used in playing the audio
    private lateinit var mAudioFocusRequest: AudioFocusRequest
    // mAudioManager is used for calling and stopping the focus requests when trying to play the audio
    private lateinit var mAudioManager: AudioManager
    // mFocusLock is a lock to stop data races betwen focus request and change.
    private val mFocusLock = Any()
    // playDelay is to indicate a delay while playing audio
    private var playDelay : Boolean = false
    // focus is used for indicating whether there is focus to play audio or not
    private var focus : Boolean = false

    // used for the firebase storage
    private lateinit var mStorage: StorageReference
    // used to name the audio file
    private lateinit var fileName: String

    // These two are for user info, helps to determine if user is native
    private lateinit var uid: String
    private lateinit var username : String
    // used to make the audio names distinct
    private lateinit var wordID : String

    // Used for adding the recording to firebase
    private lateinit var mDatabaseRecordings : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_audio)

        // used for knowing whether the user is native
        uid = intent.getStringExtra(USER_ID_KEY).toString()
        username = intent.getStringExtra(USER_NAME).toString()
        //used to make the audio file name unique
        wordID = intent.getStringExtra(WORD_ID_KEY).toString()

        //used for uploading recordings to firebase
        mDatabaseRecordings = FirebaseDatabase.getInstance().getReference("RecordingList").child(wordID)

        // for local storage
        fileName = application.getExternalFilesDir(null)?.absolutePath + "/" + System.currentTimeMillis() + ".3gp"

        // for handling audio focusing
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // for firebase storage
        mStorage = FirebaseStorage.getInstance().reference

        // functionality detailed above
        startRecordingBtn = findViewById(R.id.imageView_mic)
        playRecordingBtn = findViewById(R.id.createAudioPlayButton)
        submitBtn = findViewById(R.id.addRecordingButton)
        ringView = findViewById(R.id.imageView_ring)

        // to hide the blue ring image view when the activity is created
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

        // for building the type of audio played
        mAudioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()

        // for the audio focus request when trying to play
        mAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(mAudioAttributes).setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this, Handler(Looper.getMainLooper()))
            .setOnAudioFocusChangeListener(this, Handler(Looper.getMainLooper()))
            .build()



        // Click listener for the microphone image view, and the two main bits of functionality here
        // is to start the recording and stop. The blue ring around the microphone indicates recording
        // is currently taking place.
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

        // The listener for the play button has two main uses, to play the audio and to stop it.
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


    // used for playing the audio, and happens alongside the focus change.
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

    // used for playing the audio and knowing the quality.
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

    // used if there is a need to get permissions for those required to record audio and store locally.
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
        private const val USER_NAME = "USERNAME"
        private const val USER_ID_KEY = "USER_ID"
        private const val WORD_ID_KEY = "WORD_ID"
    }

}