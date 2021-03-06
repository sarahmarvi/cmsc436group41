package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

// This class is for the welcome screen, in which a user selects the choice to continue as a learner or
// a native speaker.

class MainActivity : AppCompatActivity() {

    private lateinit var learnerButton: Button
    private lateinit var nativeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome)

        learnerButton = findViewById(R.id.learnerBtn)
        nativeButton = findViewById(R.id.nativeBtn)

        learnerButton.setOnClickListener {
            startLearner()
        }
        nativeButton.setOnClickListener {
            startNative()
        }
    }

    private fun startLearner() {
        val intent = Intent(this@MainActivity, LanguageListActivity::class.java)
        intent.putExtra(USERNAME_KEY, LEARNER_TEXT)
        startActivity(intent)
    }

    private fun startNative() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    // To allow a user to exit/sign out without letting them use the original function of the back
    // button to go back (to a page exclusive to users signed in) after signing out.
    override fun onBackPressed() {
        this.finishAffinity()
    }

    companion object {
        const val USERNAME_KEY = "USERNAME"
        const val LEARNER_TEXT = "Learner"
    }

}