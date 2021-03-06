package com.example.audiodictionary

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// This class helps populate the list of words where a list of words is taken into WordList and
// the listView is filled with them.

// Adapted from Lab7-Firebase, Lab5 for populating the list view in NativeLanguage and LearnerLanguage
class WordList (private val context: Activity, private var words: List<Word>) : ArrayAdapter<Word>(context,
    R.layout.vocab_list, words) {

    @SuppressLint("InflateParams", "ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.vocab_list, null, true)

        val textViewName = listViewItem.findViewById<View>(R.id.vocab_word) as TextView

        val word = words[position]
        textViewName.text = word.original + " - " + word.translation

        return listViewItem
    }
}