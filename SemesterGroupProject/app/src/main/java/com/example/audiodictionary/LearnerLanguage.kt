package com.example.audiodictionary

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.google.firebase.database.*
import java.lang.Exception

// This class is responsible for populating the layout seen by a learner, which involves getting
// the words from firebase, and more detailed below.

class LearnerLanguage : AppCompatActivity(), SearchView.OnQueryTextListener {

    internal lateinit var mListViewWords: ListView
    internal lateinit var words : MutableList<Word>
    internal lateinit var wordsId : MutableList<String>

    private lateinit var mDatabaseLanguage : DatabaseReference
    private lateinit var mDatabaseWords : DatabaseReference

    private var mTitle: TextView? = null

    private lateinit var langCode : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learner_language)

        langCode = intent.getStringExtra("LANGUAGE")!!

        words = ArrayList()
        wordsId = ArrayList()

        mDatabaseLanguage = FirebaseDatabase.getInstance().getReference("Languages").child(langCode)
        mDatabaseWords = FirebaseDatabase.getInstance().getReference("Words").child(langCode)

        mTitle = findViewById(R.id.language_native_title)
        mListViewWords = findViewById(R.id.vocabList)

        setTitle()

        mListViewWords.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val wordId = wordsId[i]
            val word = words[i]

            val clickIntent = Intent(applicationContext, LearnerWordActivity::class.java)


            clickIntent.putExtra(LANGUAGE_KEY, langCode)
            clickIntent.putExtra(WORD_ID_KEY, wordId)
            clickIntent.putExtra(ORIGINAL_KEY, word.original)
            clickIntent.putExtra(TRANSLATION_KEY, word.translation)
            startActivity(clickIntent)
        }
    }



    // For creating the option menu at the top, involving searching and exiting
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        // Display search on this page
        menu.findItem(R.id.search).isVisible = true

        // word searches
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconifiedByDefault = false
        }.setOnQueryTextListener(this)

        return true
    }

    // For the functionality behind the option menu.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.exit_option -> {
                // Exit session button
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // For the searching of words
    override fun onQueryTextSubmit(search: String?): Boolean {
        val intent = Intent(this@LearnerLanguage, WordSearchActivity::class.java)

        intent.putExtra(SearchManager.QUERY, search)
        intent.putExtra("SEARCH_LANG", langCode)
        intent.action = Intent.ACTION_SEARCH
        startActivity(intent)

        Log.i(TAG, "Starting search of words starting with `${search}'...")
        return true
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        return false
    }

    // Adapted from Lab7-Firebase
    override fun onStart() {
        super.onStart()

        mDatabaseWords.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                words.clear()
                wordsId.clear()

                var word : Word? = null
                for (postSnapshot in dataSnapshot.children) {
                    try {
                        word = postSnapshot.getValue(Word::class.java)
                        postSnapshot.key?.let { wordsId.add(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    } finally {
                        words.add(word!!)
                    }
                }
                val wordListAdapter = WordList(this@LearnerLanguage, words)
                mListViewWords.adapter = wordListAdapter

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }

    // To fill the placeholder name at the top.
    private fun setTitle() {
        mDatabaseLanguage.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                val language : Language? = dataSnapshot.getValue(Language::class.java)

                mTitle!!.text = language!!.nativeName
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })

        Log.d(TAG, "Completed Set Title")
    }

    // To allow a user to exit/sign out without letting them use the original function of the back
    // button to go back (to a page exclusive to users signed in) after signing out.
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val TAG = "LearnerLanguage"
        const val LANGUAGE_KEY = "LANGUAGE"
        const val WORD_ID_KEY = "WORD_ID"
        const val ORIGINAL_KEY = "ORIGINAL"
        const val TRANSLATION_KEY = "TRANSLATION"
    }
}
