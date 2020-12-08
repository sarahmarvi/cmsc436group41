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

// This class is responsible for populating the layout seen by a native, which involves getting
// the words from firebase, and more detailed below.

class NativeLanguage : AppCompatActivity(), SearchView.OnQueryTextListener {

    internal lateinit var mListViewWords: ListView
    internal lateinit var words : MutableList<Word>

    private lateinit var mDatabaseLanguage : DatabaseReference
    private lateinit var mDatabaseWords : DatabaseReference
    internal lateinit var wordsId : MutableList<String>

    private var mTitle: TextView? = null
    private var mAddLanguageTitle: TextView? = null
    private var mEnglishTranslationTV: EditText? = null
    private var mOriginalWordTV: EditText? = null
    private var mAddBtn : Button? = null

    private lateinit var langCode : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.native_language)

        words = ArrayList()
        wordsId = ArrayList()

        val uid = intent.getStringExtra(USER_ID_KEY).toString()
        val user = intent.getStringExtra(USERNAME_KEY).toString()
        langCode = intent.getStringExtra(LANGUAGE_KEY).toString()


        mDatabaseLanguage = FirebaseDatabase.getInstance().getReference(LANGUAGE_TEXT).child(langCode)

        mDatabaseWords = FirebaseDatabase.getInstance().getReference(WORDS_TEXT).child(langCode)

        mTitle = findViewById(R.id.language_learner_title)
        mAddLanguageTitle = findViewById(R.id.native_language_add_lang)
        mEnglishTranslationTV = findViewById(R.id.editTextTextPersonName)
        mOriginalWordTV = findViewById(R.id.editTextTextPersonName2)
        mListViewWords = findViewById(R.id.vocabList)
        mAddBtn = findViewById(R.id.nativeAddWordButton)

        setTitles()

        mAddBtn!!.setOnClickListener { addNewWord() }

        mListViewWords.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val wordId = wordsId[i]
            val word = words[i]

            val clickIntent = Intent(applicationContext, NativeWordActivity::class.java)

            clickIntent.putExtra(USERNAME_KEY, user)
            clickIntent.putExtra(USER_ID_KEY, uid)
            clickIntent.putExtra(LANGUAGE_KEY, langCode)
            clickIntent.putExtra(WORD_ID_KEY, wordId)
            clickIntent.putExtra(ORIGINAL_KEY, word.original)
            clickIntent.putExtra(TRANSLATION_KEY, word.translation)
            startActivity(clickIntent)
        }

    }

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

    override fun onQueryTextSubmit(search: String?): Boolean {
        val intent = Intent(this@NativeLanguage, WordSearchActivity::class.java)

        intent.putExtra(SearchManager.QUERY, search)
        intent.putExtra(SEARCH_LANG_KEY, langCode)
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
                val language : Language? = dataSnapshot.getValue(Language::class.java)

                mAddLanguageTitle!!.text = language?.displayName
                mTitle!!.text = language?.nativeName
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }

    // Adds the new word that the user created to the database
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

        val id = mDatabaseWords.push().key
        val word = Word(originalWord, englishTranslation)
        if (id != null) {
            mDatabaseWords.child(id).setValue(word)
        }

        mEnglishTranslationTV!!.setText("")
        mOriginalWordTV!!.setText("")

        Log.i(TAG, "Added username to database")
    }

    // To allow a user to exit/sign out without letting them use the original function of the back
    // button to go back (to a page exclusive to users signed in) after signing out.
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val TAG = "NativeLanguage"
        const val USERNAME_KEY = "USERNAME"
        const val USER_ID_KEY = "USER_ID"
        const val LANGUAGE_KEY = "LANGUAGE"
        const val WORD_ID_KEY = "WORD_ID"
        const val ORIGINAL_KEY = "ORIGINAL"
        const val TRANSLATION_KEY = "TRANSLATION"
        const val LANGUAGE_TEXT = "Languages"
        const val WORDS_TEXT = "Words"
        const val SEARCH_LANG_KEY = "SEARCH_LANG"
    }
}
