package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.google.firebase.database.*
import java.lang.Exception

class NativeLanguage : AppCompatActivity() {

    internal lateinit var listViewWords: ListView
    internal lateinit var words : MutableList<Word>

    private lateinit var databaseLanguage : DatabaseReference
    private lateinit var databaseWords : DatabaseReference

    private var englishTranslationTV: EditText? = null
    private var originalWordTV: EditText? = null
    private var addBtn : Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.native_language)

        words = ArrayList()

        Log.d("NativeLanguage", "In onCreate")

        databaseWords =  FirebaseDatabase.getInstance().getReference("Languages").child(
            intent.getStringExtra("LANGUAGE").toString()).child("words")

        Log.d("NativeLanguage", "After querying database")

//        databaseWords = databaseLanguage.child("words")

        englishTranslationTV = findViewById(R.id.editTextTextPersonName)
        originalWordTV = findViewById(R.id.editTextTextPersonName2)
        listViewWords = findViewById(R.id.vocabList)
        addBtn = findViewById(R.id.button)

        addBtn!!.setOnClickListener { addNewWord() }

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

    // Adapted from Lab7-Firebase
    override fun onStart() {
        super.onStart()

        Log.d("NativeLanguage", "In onStart")

        databaseWords.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot : DataSnapshot) {
                words.clear()

                var word : Word? = null
                for (postSnapshot in dataSnapshot.children) {
                    try {
                        word = postSnapshot.getValue(Word::class.java)
                    } catch (e: Exception) {
                        Log.e("NativeLanguage", e.toString())
                    } finally {
                        words.add(word!!)
                    }
                }
                val wordListAdapter = WordList(this@NativeLanguage, words)
                listViewWords.adapter = wordListAdapter

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // do nothing
            }
        })
    }

    private fun addNewWord() {

        val englishTranslation = englishTranslationTV!!.text.toString()
        val originalWord = originalWordTV!!.text.toString()

        if (!TextUtils.isEmpty(englishTranslation) && !TextUtils.isEmpty(originalWord)) {

            val id = databaseWords.push().key

            // Creating User Object
            val word = Word(originalWord, englishTranslation, "")

            // Saving the Word
            if (id != null) {
                databaseWords.child(id).setValue(word)
            }

            Log.i("CreateAccountActivity", "Added username to database")

        }
    }
}