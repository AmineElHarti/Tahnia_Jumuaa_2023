package com.tahni2a_jomo3a_2023.tahni2ajomo3a2023

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.tahni2a_jomo3a_2023.tahni2ajomo3a2023.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val recyclerView = binding.rvPhotos
        listFiles(this@MainActivity, recyclerView)
    }

    private fun listFiles(
        context: Context,
        recyclerView: RecyclerView
    ) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val databaseRef = FirebaseDatabase.getInstance().reference
                val imageUrls = mutableListOf<String>()

                val list = databaseRef.child("images").get().await()
                for (img in list.children) {
                    val url = img.getValue(String::class.java).orEmpty()
                    imageUrls.add(url)
                }
                withContext(Dispatchers.Main) {
                    val photoAdapter = PhotoAdapter(context, imageUrls)
                    recyclerView.apply {
                        adapter = photoAdapter
                        layoutManager = LinearLayoutManager(context)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
}