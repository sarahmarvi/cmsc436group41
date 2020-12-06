package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val intent = Intent(this@MainActivity, MainActivity::class.java)
//        startActivity(intent)
        return true
    }

    private fun startLearner() {
        val intent = Intent(this@MainActivity, LanguageListActivity::class.java)
        intent.putExtra("theUser", "Learner")
        startActivity(intent)
    }

    private fun startNative() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
    }
}