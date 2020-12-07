package com.example.audiodictionary

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.google.firebase.database.*
import java.lang.Exception

class NativeLanguage : AppCompatActivity(), SearchView.OnQueryTextListener {

    internal lateinit var mListViewWords: ListView
    internal lateinit var words : MutableList<Word>

    private lateinit var mDatabaseLanguage : DatabaseReference
    private lateinit var mDatabaseWords : DatabaseReference
    internal lateinit var wordsId : MutableList<String>
    private lateinit var langCode : String

    private var mTitle: TextView? = null
    private var mAddLanguageTitle: TextView? = null
    private var mEnglishTranslationTV: EditText? = null
    private var mOriginalWordTV: EditText? = null
    private var mAddBtn : Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.native_language)

        words = ArrayList()
        wordsId = ArrayList()

        val user = intent.getStringExtra("USERNAME")
        val langCode = intent.getStringExtra("LANGUAGE")


        mDatabaseLanguage = FirebaseDatabase.getInstance().getReference("Languages").child(
            intent.getStringExtra("LANGUAGE")!!)

        mDatabaseWords = FirebaseDatabase.getInstance().getReference("Words").child(
            intent.getStringExtra("LANGUAGE")!!)

        mTitle = findViewById(R.id.language_learner_title)
        mAddLanguageTitle = findViewById(R.id.native_language_add_lang)
        mEnglishTranslationTV = findViewById(R.id.editTextTextPersonName)
        mOriginalWordTV = findViewById(R.id.editTextTextPersonName2)
        mListViewWords = findViewById(R.id.vocabList)
        mAddBtn = findViewById(R.id.button)

        setTitles()

        mAddBtn!!.setOnClickListener { addNewWord() }

        mListViewWords.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val wordId = wordsId[i]
            val word = words[i]
            val clickIntent : Intent = Intent(applicationContext, NativeWordActivity::class.java)

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
        val intent = Intent(this@NativeLanguage, WordSearchActivity::class.java)

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

        Log.d(TAG, "In onStart")

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
                val wordListAdapter = WordList(this@NativeLanguage, words)
                mListViewWords.adapter = wordListAdapter

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }

    private fun setTitles() {
        mDatabaseLanguage.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                var language : Language? = dataSnapshot.getValue(Language::class.java)

                mAddLanguageTitle!!.text = language!!.displayName
                mTitle!!.text = language!!.nativeName
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }

    private fun addNewWord() {

        val englishTranslation = mEnglishTranslationTV!!.text.toString()
        val originalWord = mOriginalWordTV!!.text.toString()

        if (TextUtils.isEmpty(englishTranslation)) {
            Toast.makeText(applicationContext, "Please enter translation!", Toast.LENGTH_LONG).show()
            return
        }

        if (TextUtils.isEmpty(originalWord)) {
            Toast.makeText(applicationContext, "Please enter word!", Toast.LENGTH_LONG).show()
            return
        }


        // Generating a new id for the word
        val id = mDatabaseWords.push().key

        // Creating User Object
        val word = Word(originalWord, englishTranslation)

        // Saving the Word
        if (id != null) {
            mDatabaseWords.child(id).setValue(word)
        }

        mEnglishTranslationTV!!.setText("")
        mOriginalWordTV!!.setText("")

        Log.i(TAG, "Added username to database")

    }

    companion object {
        const val TAG = "NativeLanguage"
    }
}