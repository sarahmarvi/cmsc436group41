package com.example.audiodictionary

import android.annotation.SuppressLint
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

// This class serves to populate the list of languages for languages.xml

class LanguageListActivity : AppCompatActivity() {

    private lateinit var mGreetingTextView: TextView
    private lateinit var mListViewLanguages: ListView
    private lateinit var mDatabaseLanguage : DatabaseReference

    internal lateinit var languages : MutableList<Language>
    internal lateinit var languageCodes : MutableList<String>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.languages)

        languages = ArrayList()
        languageCodes = ArrayList()

        val uid = intent.getStringExtra(USER_ID_KEY).toString()
        val user = intent.getStringExtra(USERNAME_KEY).toString()

        mListViewLanguages = findViewById(R.id.language_list)
        mGreetingTextView = findViewById(R.id.languages_greeting)
        mGreetingTextView.text = WELCOME_TEXT + user

        mDatabaseLanguage =  FirebaseDatabase.getInstance().getReference(LANGUAGES_TEXT)

        mListViewLanguages.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val langCode = languageCodes[i]

            val clickIntent : Intent = if (user == LEARNER_NAME) {
                Intent(applicationContext, LearnerLanguage::class.java)
            } else {
                Intent(applicationContext, NativeLanguage::class.java)
                .putExtra(USER_ID_KEY, uid)
            }

            clickIntent.putExtra(LANGUAGE_KEY, langCode)
            clickIntent.putExtra(USERNAME_KEY, user)

            startActivity(clickIntent)
        }
    }

    // used to create the menu options
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        // Hide search on this page
        menu.findItem(R.id.search).isVisible = false
        return true
    }

    // Allows a user to search for a word and exit/sign out to welcome screen using the option menu
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

    // Adapted from Lab7-Firebase for getting the languages
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
                        Log.e(TAG, e.toString())
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

    // To allow a user to exit/sign out without letting them use the original function of the back
    // button to go back (to a page exclusive to users signed in) after signing out.
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val TAG = "LanguageListActivity"
        const val LANGUAGE_KEY = "LANGUAGE"
        const val USER_ID_KEY = "USER_ID"
        const val USERNAME_KEY = "USERNAME"
        const val LEARNER_NAME = "Learner"
        const val LANGUAGES_TEXT = "Languages"
        const val WELCOME_TEXT = "Welcome, "

    }

}
