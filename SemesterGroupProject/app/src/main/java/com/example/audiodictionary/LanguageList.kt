package com.example.audiodictionary

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// Adapted from Lab7-Firebase to populate the list adapter in LanguageList Activity
class LanguageList (private val context: Activity, private var languages: List<Language>) : ArrayAdapter<Language>(context,
    R.layout.specified_language, languages) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.specified_language, null, true)

        val textViewName = listViewItem.findViewById<View>(R.id.textView13) as TextView

        val language = languages[position]
        textViewName.text = language.nativeName + " - " + language.displayName

        return listViewItem
    }
}