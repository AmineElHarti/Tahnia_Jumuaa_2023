package com.tahni2a_jomo3a_2023.tahni2ajomo3a2023

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.tahni2a_jomo3a_2023.tahni2ajomo3a2023.LanguageList.Companion.changeLanguage

class LanguageAdapter(context: Context, private val languageList: List<LanguageItem>) :
    ArrayAdapter<LanguageItem>(context, 0, languageList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.language_spinner_view, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val language = getItem(position)
        val view = LayoutInflater.from(context).inflate(R.layout.language_drop_down_item,parent,false)
        val languageFlag = view.findViewWithTag<ImageView>("language_flag")
        val languageName = view.findViewWithTag<TextView>("language_name")
        languageFlag.setImageResource(language!!.icon)
        languageName.text=language.name
        view.setOnClickListener {
            changeLanguage(context, language.abbreviation)
        }
        return view
    }
}