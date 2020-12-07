package com.example.audiodictionary

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.database.*
import java.lang.Exception

class WordSearchActivity : AppCompatActivity() {
    private lateinit var mDatabaseWords : DatabaseReference
    private lateinit var mListViewWords: ListView
    private lateinit var mTitle: TextView
    private lateinit var wordsId: MutableList<String>
    private lateinit var words: MutableList<Word>
    private lateinit var langCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "Creating WordSearchActivity!")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learner_language)
        mTitle = findViewById(R.id.language_native_title)
        mTitle.text = getString(R.string.search_title)
        mListViewWords = findViewById(R.id.vocabList)
        mListViewWords.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val wordId = wordsId[i]
            val word = words[i]

            val clickIntent = Intent(applicationContext, LearnerWordActivity::class.java)
            clickIntent.putExtra("LANGUAGE", langCode)
            clickIntent.putExtra("WORD_ID", wordId)
            clickIntent.putExtra("ORIGINAL", word.original)
            clickIntent.putExtra("TRANSLATION", word.translation)
            startActivity(clickIntent)
        }

        // used to check the word searches
        mDatabaseWords = FirebaseDatabase.getInstance().getReference("Words")

        // hold the words
        words = ArrayList()
        wordsId = ArrayList()

        // handle search intents
        onSearchIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        onSearchIntent(intent)
    }

    private fun onSearchIntent(intent: Intent) {
        Log.i(TAG, "Just got a new search intent!")
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                //doMySearch(query)
                langCode = intent.getStringExtra("SEARCH_LANG")!!
                Log.i(TAG, "Received language code: $langCode")
                searchWord(query, langCode)
            }
        }
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

    // This searches using the word and the 2-letter language key
    private fun searchWord(word: String, language: String) {
        val wordQuery = mDatabaseWords.child(language).orderByChild("original").startAt(word).endAt(word + "uf8ff")
        Log.i(TAG, "Seeing which words start with `$word'...")
        wordQuery.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var wordResult: Word?
                for (postSnapshot in dataSnapshot.children) {
                    try {
                        wordResult = postSnapshot.getValue(Word::class.java)
                        postSnapshot.key?.let {
                            Log.i(TAG, "Obtained word ${wordResult!!}!")
                            wordsId.add(postSnapshot.key!!)
                            words.add(wordResult)
                        }
                    } catch (e: Exception) {
                        Log.e(NativeLanguage.TAG, e.toString())
                    }
                }
                val wordListAdapter = WordList(this@WordSearchActivity, words)
                mListViewWords.adapter = wordListAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
        Log.i(TAG, "The words retrieved from $language: $words")
        //Log.i(TAG, "The words retrieved from $language")
    }

    companion object {
        const val TAG = "WordSearchActivity"
    }
}
