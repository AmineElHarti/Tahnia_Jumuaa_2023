package com.tahni2a_jomo3a_2023.tahni2ajomo3a2023

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.content.ContextCompat
import java.util.*

class LanguageList {
    companion object{
        fun getLanguageList(): List<LanguageItem> {
            return arrayListOf(
                LanguageItem("Arabic", "ar", R.drawable.arabic),
                LanguageItem("French", "fr", R.drawable.french),
                LanguageItem("English", "en", R.drawable.english)
            )
        }
        fun changeLanguage(context: Context, abr:String) {
            (context as MainActivity).saveData("SAVED_LOCAL",abr)
            val locale = Locale(abr)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
//        defaultResources = Resources(defaultResources.assets, defaultResources.displayMetrics,config)
            context.resources.updateConfiguration(config, Resources.getSystem().displayMetrics)
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            ContextCompat.startActivity(context, intent, null)
        }
    }
}
