package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

class LearnerLanguage : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learner_language)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        return true
    }


}