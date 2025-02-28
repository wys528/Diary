package com.example.myapplication.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.database.Diary
import com.example.myapplication.database.DiaryDatabase
import com.example.myapplication.databinding.ActivityAddDiaryBinding
import com.example.myapplication.model.CityResponse
import com.example.myapplication.model.WeatherResponse
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.WeatherApi
import com.example.myapplication.utils.DateUtils
import com.example.myapplication.utils.LocationUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.concurrent.Executors

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDiaryBinding
    private lateinit var diaryDatabase: DiaryDatabase
    private lateinit var locationUtils: LocationUtils
    private var selectedLocalImageUri: Uri? = null
    private var networkImageUrl: String? = null
    private val API_KEY = "4de3eec7beea433298524d3955c9d13e"
    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        diaryDatabase = DiaryDatabase.getInstance(this)
        locationUtils = LocationUtils(this)

        binding.selectImageButton.setOnClickListener {
            pickLocalImage()
        }


        binding.addNetworkImageButton.setOnClickListener {
            networkImageUrl = binding.networkImageUrlEditText.text.toString()
            val uri = Uri.parse(networkImageUrl)
            if (networkImageUrl?.isNotEmpty() == true) {
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_launcher_background) // 加载过程中显示的占位符图片
                    .apply(RequestOptions.circleCropTransform())
                    .error(R.drawable.ic_launcher_foreground) // 加载失败时显示的图片
                    .into(binding.selectedImageView)
                binding.selectedImageView.visibility = android.view.View.VISIBLE
            }
        }


        binding.saveDiaryButton.setOnClickListener {
            saveDiary()
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
        }

        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            getLocation()
        }
    }

    private fun pickLocalImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedLocalImageUri = data.data
            Glide.with(this)
                .load(selectedLocalImageUri)
                .into(binding.selectedImageView)
            binding.selectedImageView.visibility = android.view.View.VISIBLE
        }
    }

    private fun getLocation() {
        locationUtils.getLocation { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            Log.d("AddDiaryActivity", "获取到的经纬度: 纬度 $latitude, 经度 $longitude")
            val loc = String.format("%.2f,%.2f", location!!.longitude, location!!.latitude)
            Executors.newSingleThreadExecutor().execute {
                runOnUiThread {
                    binding.locationTextView.text = loc
                    getCityId(loc)
                }
            }
        }
    }


    private fun showToastOnUiThread(message: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCityId(cityName: String) {
        val weatherApi = RetrofitClient.geoInstance.create(WeatherApi::class.java)
        weatherApi.getCityInfo(API_KEY, cityName).enqueue(object : Callback<CityResponse> {
            override fun onResponse(call: Call<CityResponse>, response: Response<CityResponse>) {
                if (response.isSuccessful && response.body()?.code == "200") {
                    val cityLocation = response.body()?.location?.firstOrNull()
                    if (cityLocation != null) {
                        binding.locationTextView.text = cityLocation.name

                        getWeatherInfo(cityLocation.id)
                    }
                } else {
                    Log.e("AddDiaryActivity", "获取城市 ID 失败: ${response.message()}, 响应代码: ${response.code()}, 响应体: ${response.errorBody()?.string()}")
                    showToastOnUiThread("获取城市 ID 失败")
                }
            }

            override fun onFailure(call: Call<CityResponse>, t: Throwable) {
                Log.e("AddDiaryActivity", "获取城市 ID 网络请求失败: ${t.message}", t)
                showToastOnUiThread("获取城市 ID 网络请求失败")
            }
        })
    }

    private fun getWeatherInfo(cityId: String) {
        val weatherApi = RetrofitClient.weatherInstance.create(WeatherApi::class.java)
        weatherApi.getWeatherInfo(API_KEY, cityId).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful && response.body()?.code == "200") {
                    val today = DateUtils.formatDate(Calendar.getInstance().time)
                    val todayWeather = response.body()?.daily?.firstOrNull { it.fxDate == today }
                    if (todayWeather != null) {
                        val weatherText = todayWeather.textDay
                        binding.weatherTextView.text = weatherText
                    }
                } else {
                    Log.e("AddDiaryActivity", "获取天气信息失败: ${response.message()}, 响应代码: ${response.code()}, 响应体: ${response.errorBody()?.string()}")
                    showToastOnUiThread("获取天气信息失败")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("AddDiaryActivity", "获取天气信息网络请求失败: ${t.message}", t)
                showToastOnUiThread("获取天气信息网络请求失败")
            }
        })
    }


    private fun saveDiary() {
        Executors.newSingleThreadExecutor().execute {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()
            val localImagePath = selectedLocalImageUri?.toString()
            val date = DateUtils.formatDate(Calendar.getInstance().time)
            val weather = binding.weatherTextView.text.toString()
            val location = binding.locationTextView.text.toString()

            val diary = Diary(
                title = title,
                content = content,
                localImagePath = localImagePath,
                networkImageUrl = networkImageUrl,
                date = date,
                weather = weather,
                location = location
            )

            diaryDatabase.diaryDao().insertDiary(diary)
            runOnUiThread {
                Toast.makeText(this, "日记保存成功", Toast.LENGTH_SHORT).show()
                val intent = Intent("com.example.app.DIARY_SAVED")
                sendBroadcast(intent)
                finish()
            }
        }
    }
}