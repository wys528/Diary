package com.example.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.activity.DiaryDetailActivity
import com.example.myapplication.database.Diary
import com.example.myapplication.databinding.DiaryItemBinding

class DiaryAdapter (
    private val context: Context,
    private val diaries: List<Diary>
): RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>(){

    inner class DiaryViewHolder(val binding: DiaryItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = diaries.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = DiaryItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DiaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diary = diaries[position]
        holder.binding.diaryTitle.text = diary.title
        holder.binding.diaryContent.text = diary.content
        holder.binding.diaryDate.text = diary.date
        holder.binding.diaryPlace.text = diary.location
        holder.binding.diaryWeather.text = diary.weather

        if(diary.localImagePath != null){
            holder.binding.diaryImage.visibility = View.VISIBLE
            Glide.with(context)
                .load(diary.localImagePath)
                .into(holder.binding.diaryImage)
        }else if(diary.networkImageUrl != null){
            holder.binding.diaryImage.visibility = View.VISIBLE
            Glide.with(context)
                .load(diary.networkImageUrl)
                .into(holder.binding.diaryImage)
        }else{
            holder.binding.diaryImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DiaryDetailActivity::class.java)
            intent.putExtra("diaryId",diary.id)
            context.startActivity(intent)
        }
    }

}