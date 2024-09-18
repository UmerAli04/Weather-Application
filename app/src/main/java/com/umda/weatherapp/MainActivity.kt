package com.umda.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.SearchView
import android.widget.Toast
import com.umda.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        //Call Fetch weather data function
        fetchWeatherData("Karachi")
        //Call SearchCity function
        SearchCity()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun SearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        //Set up Retrofit
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/").build()
            .create(ApiInterface::class.java)
        val response =
            retrofit.getWeatherData(cityName, "f42017e7967c380b7bb8ca171fa5e6b1", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunset = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.sea_level
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val maxTemp = responseBody.main.temp_max.toString()
                    val minTemp = responseBody.main.temp_min.toString()
                    //Set up UI
                    binding.temperature.text = "$temperature°C"
                    binding.weather.text = condition
                    binding.maxTemp.text = "Max : $maxTemp°C"
                    binding.minTemp.text = "Min : $minTemp°C"
                    binding.humidity.text = "$humidity%"
                    binding.windSpeed.text = "$windSpeed m/s"
                    binding.sunrise.text = "${time(sunrise)}"
                    binding.sunset.text = "${time(sunset)}"
                    binding.sea.text = "$seaLevel hPa"
                    binding.condition.text = condition
                    binding.cityName.text = "$cityName"
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = dateName()
                    //Create a function to change images according to weather condition
                    changeImagesAccordingToWeather(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                // Handle network error
                Log.e("MainActivity", "Error fetching weather data", t)
                Toast.makeText(this@MainActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
            }

        })
    }

    //Function to change images according to weather condition
    private fun changeImagesAccordingToWeather(condition: String) {
        when (condition) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_bg)
                binding.lottieAnimationView.setAnimation(R.raw.sun_anim)
            }
            "Haze", "Partly Clouds", "Mist", "Fog", "Clouds", "Smoke", "Overcast" ->  {
                binding.root.setBackgroundResource(R.drawable.cloud_bg)
                binding.lottieAnimationView.setAnimation(R.raw.cloud_anim)
                binding.lottieAnimationView.setAnimation(R.raw.clouds_anim)
            }
            "Drizzle", "Rain", "Light Rain", "Thunderstorm", "Showers", "Heavy Rain", "Moderate Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_bg)
                binding.lottieAnimationView.setAnimation(R.raw.rain_anim)
            }
            "Snow", "Light Snow", "Heavy Snow", "Blizzard", "Moderate Snow" -> {
                binding.root.setBackgroundResource(R.drawable.snow_bg)
                binding.lottieAnimationView.setAnimation(R.raw.snow_anim)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_bg)
                binding.lottieAnimationView.setAnimation(R.raw.sun_anim)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    //Function to get day name
    fun dayName(timeStamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    //Function to get date
    private fun dateName(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    //Function to get time stamp
    private fun time(timeStamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timeStamp * 1000))
    }
}