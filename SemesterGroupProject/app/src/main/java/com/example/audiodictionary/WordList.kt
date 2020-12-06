package com.example.audiodictionary

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

// Adapted from Lab7-Firebase
class WordList (private val context: Activity, private var words: List<Word>) : ArrayAdapter<Word>(context,
    R.layout.vocab_list, words) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.vocab_list, null, true)

        val textViewName = listViewItem.findViewById<View>(R.id.vocab_word) as TextView

        val word = words[position]
        textViewName.text = word.original + " - " + word.translation

        return listViewItem
    }
}