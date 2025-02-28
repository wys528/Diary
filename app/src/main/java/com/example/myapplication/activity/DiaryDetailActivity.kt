package com.example.myapplication.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.database.DiaryDatabase
import com.example.myapplication.databinding.ActivityAddDiaryBinding
import com.example.myapplication.databinding.ActivityDiaryDetailBinding
import java.util.concurrent.Executors

class DiaryDetailActivity : AppCompatActivity() {



    private lateinit var binding: ActivityDiaryDetailBinding
    private lateinit var diaryDatabase: DiaryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        diaryDatabase = DiaryDatabase.getInstance(this)

        val diaryId = intent.getIntExtra("diaryId", -1)
        if (diaryId != -1) {
            loadDiaryDetails(diaryId)
        }
    }

    private fun loadDiaryDetails(diaryId: Int) {
        Executors.newSingleThreadExecutor().execute {
            val diary = diaryDatabase.diaryDao().getDiaryById(diaryId)
            runOnUiThread {
                binding.diaryTitle.text = diary.title
                binding.diaryContent.text = diary.content
                binding.diaryDate.text = diary.date
                binding.diaryPlace.text = diary.location
                binding.diaryWeather.text = diary.weather


                if (diary.localImagePath != null) {
                    Glide.with(this@DiaryDetailActivity)
                        .load(diary.localImagePath)
                        .into(binding.diaryImage)
                    binding.diaryImage.visibility = android.view.View.VISIBLE
                } else if (diary.networkImageUrl != null) {
                    Glide.with(this@DiaryDetailActivity)
                        .load(diary.networkImageUrl)
                        .into(binding.diaryImage)
                    binding.diaryImage.visibility = android.view.View.VISIBLE
                } else {
                    binding.diaryImage.visibility = android.view.View.GONE
                }
            }
        }
    }
}