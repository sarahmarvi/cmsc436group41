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

class LearnerLanguage : AppCompatActivity(), SearchView.OnQueryTextListener {

    internal lateinit var mListViewWords: ListView
    internal lateinit var words : MutableList<Word>
    internal lateinit var wordsId : MutableList<String>

    private lateinit var mDatabaseLanguage : DatabaseReference
    private lateinit var mDatabaseWords : DatabaseReference
    private lateinit var langCode : String

    private var mTitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learner_language)

        val intent = getIntent() as Intent
        val user = intent.getStringExtra("USERNAME").toString()
        langCode = intent.getStringExtra("LANGUAGE").toString()

        words = ArrayList()
        wordsId = ArrayList()

        mDatabaseLanguage = FirebaseDatabase.getInstance().getReference("Languages").child(langCode)
        mDatabaseWords = FirebaseDatabase.getInstance().getReference("Words").child(langCode)

        mTitle = findViewById(R.id.language_native_title)
        mListViewWords = findViewById(R.id.vocabList)

        setTitle()

        mListViewWords.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val wordId = wordsId[i]
            val word = words[i]

            val clickIntent : Intent = Intent(applicationContext, LearnerWordActivity::class.java)


            clickIntent.putExtra("LANGUAGE", langCode)
            clickIntent.putExtra("WORD_ID", wordId)
            clickIntent.putExtra("ORIGINAL", word.original)
            clickIntent.putExtra("TRANSLATION", word.translation)
            startActivity(clickIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        // word searches
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconifiedByDefault = false
        }.setOnQueryTextListener(this)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //val intent = Intent(this, MainActivity::class.java)
        //startActivity(intent)
        return true
    }

    override fun onQueryTextSubmit(search: String?): Boolean {
        val intent = Intent(this@LearnerLanguage, WordSearchActivity::class.java)

        intent.putExtra(SearchManager.QUERY, search)
        intent.putExtra("SEARCH_LANG", langCode)
        intent.setAction(Intent.ACTION_SEARCH)
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

    private fun setTitle() {
        mDatabaseLanguage.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                var language : Language? = dataSnapshot.getValue(Language::class.java)

                mTitle!!.text = language!!.nativeName
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })

        Log.d(TAG, "Completed Set Title")
    }

    companion object {
        const val TAG = "LearnerLanguage"
    }
}