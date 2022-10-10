package com.tahni2a_jomo3a_2023.tahni2ajomo3a2023

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat.startActivity
import java.sql.Wrapper
import java.util.*

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
            changeLanguage(language.abbreviation)
        }
        return view
    }
    private fun changeLanguage(abr:String) {
        val locale = Locale(abr)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)

        context.resources.updateConfiguration(config,Resources.getSystem().displayMetrics)
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(context,intent,null)
    }
}