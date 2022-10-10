package com.tahni2a_jomo3a_2023.tahni2ajomo3a2023

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.database.FirebaseDatabase
import com.tahni2a_jomo3a_2023.tahni2ajomo3a2023.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
var isWriteExternalStorageGranted = false
var isReadExternalStorageGranted = false

class MainActivity : AppCompatActivity() {
    private var firstInternetCheck = true
    private lateinit var cld: ConnectionLiveData
    private lateinit var binding: ActivityMainBinding
    private val languageList = LanguageList.getLanguageList()
    lateinit var mAdView: AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        readData()
        setContentView(binding.root)
        checkNetworkConnection()
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isWriteExternalStorageGranted =
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: isWriteExternalStorageGranted
                isReadExternalStorageGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: isReadExternalStorageGranted
            }
        requestPermission(this)
        MobileAds.initialize(this)
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mAdView.isVisible = true
                super.onAdLoaded()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                mAdView.isGone = true
            }

            override fun onAdClosed() {
                mAdView.isGone = true
                super.onAdClosed()
            }
        }
        setupLanguageAdapter(this@MainActivity, languageList)
        val recyclerView = binding.rvPhotos
        listFiles(this@MainActivity, recyclerView)

    }

    override fun onStart() {
        super.onStart()
        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
    override fun onPause() {
        super.onPause()
        firstInternetCheck = true
    }

    fun saveData(keyString: String, value: String) {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putString(keyString, value)
        }.apply()
    }

    private fun readData() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val localAbr: String? = sharedPreferences.getString("SAVED_LOCAL", null)
        if (localAbr != null) {
            val locale = Locale(localAbr)
            Locale.setDefault(locale)
            val config: Configuration = baseContext.resources.configuration
            config.setLocale(locale)
        }
    }

    private fun checkNetworkConnection() {
        cld = ConnectionLiveData(application)
        cld.observe(this) { isConnected ->
            if (isConnected) {
                if (firstInternetCheck && binding.tvNotConnected.isGone) {
                    firstInternetCheck = false
                } else {
                    binding.tvConnected.isVisible = true
                    binding.tvNotConnected.isGone = true
                    object : CountDownTimer(3000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            binding.tvConnected.isGone = true
                        }
                    }.start()
                }
            } else {
                binding.tvConnected.isGone = true
                binding.tvNotConnected.isVisible = true
            }
        }
    }

    private fun setupLanguageAdapter(context: Context, languageList: List<LanguageItem>) {
        val languageAdapter = LanguageAdapter(context, languageList)
        binding.languageSpinner.adapter = languageAdapter
    }

    private fun listFiles(context: Context, recyclerView: RecyclerView) =
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

fun requestPermission(context: Context) {
    isWriteExternalStorageGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
    isReadExternalStorageGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    val permissionRequest: MutableList<String> = ArrayList()

    if (!isWriteExternalStorageGranted) {
        permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    if (!isReadExternalStorageGranted) {
        permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    if (permissionRequest.isNotEmpty()) {
        permissionLauncher.launch(permissionRequest.toTypedArray())
    }
}
