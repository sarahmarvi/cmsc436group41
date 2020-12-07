package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import java.lang.Exception

class LanguageListActivity : AppCompatActivity() {

    private lateinit var mGreetingTextView: TextView
    private lateinit var mListViewLanguages: ListView
    private lateinit var mDatabaseLanguage : DatabaseReference

    internal lateinit var languages : MutableList<Language>
    internal lateinit var languageCodes : MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.languages)

        languages = ArrayList()
        languageCodes = ArrayList()

        val uid = intent.getStringExtra("USER_ID").toString()
        val user = intent.getStringExtra("USERNAME").toString()

        mListViewLanguages = findViewById(R.id.language_list)
        mGreetingTextView = findViewById(R.id.languages_greeting)
        mGreetingTextView.text = "Welcome, " + user

        mDatabaseLanguage =  FirebaseDatabase.getInstance().getReference("Languages")

        mListViewLanguages.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val langCode = languageCodes[i]

            val clickIntent : Intent = if (user == "Learner") {
                Intent(applicationContext, LearnerLanguage::class.java)
            } else {
                Intent(applicationContext, NativeLanguage::class.java)
                .putExtra("USER_ID", uid)
            }

            clickIntent.putExtra("LANGUAGE", langCode)
            clickIntent.putExtra("USERNAME", user)

            startActivity(clickIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        // Hide search on this page
        menu.findItem(R.id.search).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                // Tell user that this page cannot use searching
                Toast.makeText(this, "Cannot search on this page.", Toast.LENGTH_LONG).show()
                return false
            }
            R.id.exit_option -> {
                // Exit session button
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    // Adapted from Lab7-Firebase
    override fun onStart() {
        super.onStart()

        mDatabaseLanguage.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                languages.clear()
                languageCodes.clear()

                var language : Language? = null
                for (postSnapshot in dataSnapshot.children) {
                    try {
                        language = postSnapshot.getValue(Language::class.java)
                        postSnapshot.key?.let { languageCodes.add(it) }
                    } catch (e: Exception) {
                        Log.e("LanguageListActivity", e.toString())
                    } finally {
                        languages.add(language!!)
                    }
                }
                val languageListAdapter = LanguageList(this@LanguageListActivity, languages)
                mListViewLanguages.adapter = languageListAdapter

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }


}
