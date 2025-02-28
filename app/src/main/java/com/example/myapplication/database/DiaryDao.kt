package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface DiaryDao {
    @Insert
    fun insertDiary(diary: Diary)

    @Query("SELECT * FROM diaries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchDiaries(query: String): List<Diary>

    @Query("SELECT * FROM diaries")
    fun getAllDiaries(): List<Diary>

    @Query("SELECT * FROM diaries WHERE id = :diaryId")
    fun getDiaryById(diaryId: Int): Diary
}