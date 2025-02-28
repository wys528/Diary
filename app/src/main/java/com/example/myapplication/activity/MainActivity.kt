package com.example.myapplication.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.DiaryAdapter
import com.example.myapplication.database.DiaryDatabase
import com.example.myapplication.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val diarySavedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == "com.example.app.DIARY_SAVED"){
                loadAllDiaries()
            }
        }
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var diaryDatabase: DiaryDatabase
    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filter = IntentFilter("com.example.app.DIARY_SAVED")
        registerReceiver(diarySavedReceiver,filter)

        diaryDatabase = DiaryDatabase.getInstance(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        diaryAdapter = DiaryAdapter(this, emptyList())
        binding.recyclerView.adapter = diaryAdapter

        loadAllDiaries()

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                searchDiaries(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.addDiaryButton.setOnClickListener {
            val intent = Intent(this, AddDiaryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadAllDiaries() {
        Executors.newSingleThreadExecutor().execute {
            val diaries = diaryDatabase.diaryDao().getAllDiaries()
            runOnUiThread {
                diaryAdapter = DiaryAdapter(this@MainActivity, diaries)
                binding.recyclerView.adapter = diaryAdapter
            }
        }
    }

    private fun searchDiaries(query: String) {
        Executors.newSingleThreadExecutor().execute {
            val diaries = diaryDatabase.diaryDao().searchDiaries(query)
            runOnUiThread {
                diaryAdapter = DiaryAdapter(this@MainActivity, diaries)
                binding.recyclerView.adapter = diaryAdapter
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(diarySavedReceiver)
    }
}

