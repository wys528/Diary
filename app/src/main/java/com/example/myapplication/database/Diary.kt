package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "diaries")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val localImagePath: String?,
    val networkImageUrl: String?,
    val date: String,
    val weather: String,
    val location: String
)