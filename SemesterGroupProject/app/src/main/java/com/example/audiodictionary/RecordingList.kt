package com.example.audiodictionary

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class RecordingList (private val context: Activity, private var recordings: List<Recording>) : ArrayAdapter<Recording>(context,
    R.layout.vocab_list, recordings) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.vocab_list, null, true)

//        val textViewFileName = listViewItem.findViewById<View>(R.id.vocab_word) as TextView
//        val textViewUserName = listViewItem.findViewById<View>(R.id.vocab_word) as TextView
//
//        val record = recordings[position]
//        textViewFileName.text = record.audioFile
//        textViewUserName.text = record.user

        return listViewItem
    }
}