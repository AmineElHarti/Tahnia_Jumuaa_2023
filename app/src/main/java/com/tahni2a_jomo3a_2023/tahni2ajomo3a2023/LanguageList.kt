package com.tahni2a_jomo3a_2023.tahni2ajomo3a2023

class LanguageList {
    companion object{
        fun getLanguageList(): List<LanguageItem> {
            return arrayListOf(
                LanguageItem("Arabic", "ar", R.drawable.arabic),
                LanguageItem("French", "fr", R.drawable.french),
                LanguageItem("English", "en", R.drawable.english)
            )
        }
    }
}
