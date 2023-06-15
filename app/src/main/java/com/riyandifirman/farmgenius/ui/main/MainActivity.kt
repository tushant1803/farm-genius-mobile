package com.riyandifirman.farmgenius.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.riyandifirman.farmgenius.R
import com.riyandifirman.farmgenius.adapter.DetectDiseaseHistoryAdapter
import com.riyandifirman.farmgenius.databinding.ActivityMainBinding
import com.riyandifirman.farmgenius.network.ApiConfig
import com.riyandifirman.farmgenius.network.responses.Data
import com.riyandifirman.farmgenius.network.responses.GetHistoryResponseItem
import com.riyandifirman.farmgenius.network.responses.LoginResponse
import com.riyandifirman.farmgenius.network.responses.RegisterResponse
import com.riyandifirman.farmgenius.ui.detection.DetectionActivity
import com.riyandifirman.farmgenius.ui.history.HistoryActivity
import com.riyandifirman.farmgenius.ui.history.HistoryResultDetectionActivity
import com.riyandifirman.farmgenius.ui.profile.ProfileActivity
import com.riyandifirman.farmgenius.ui.recomendation.RecomendationActivity
import com.riyandifirman.farmgenius.util.Preferences
import com.riyandifirman.farmgenius.viewmodel.MainViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var helloName: TextView
    private lateinit var detectCounter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        helloName = binding.helloUser
        detectCounter = binding.jumlahTerdeteksi

        // Inisialisasi ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.init(this)

        viewModel.name.observe(this) { name ->
             helloName.text = "Halo, $name!"
        }

        setupRecyclerView()

        // Set button listener
        val profileButton = binding.rectangleProfile
        profileButton.setOnClickListener{
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        val recomendationButton = binding.cariButton
        recomendationButton.setOnClickListener{
            val intent = Intent(this@MainActivity, RecomendationActivity::class.java)
            startActivity(intent)
        }

        val historyButton = binding.lihatLebih
        historyButton.setOnClickListener{
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }

        val detectionButton = binding.fab
        detectionButton.setOnClickListener{
            val intent = Intent(this@MainActivity, DetectionActivity::class.java)
            startActivity(intent)
        }
    }

    // fungsi untuk menampilkan data history penyakit
    fun setupRecyclerView() {
        // Mendapatkan data list dari API
        val token = "Bearer " + viewModel.token
        val client = ApiConfig.getApiService().getHistoryDisease(token)
        client.enqueue(object : Callback<List<GetHistoryResponseItem>> {
            override fun onResponse(
                call: Call<List<GetHistoryResponseItem>>,
                response: Response<List<GetHistoryResponseItem>>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val detectDiseaseItems = responseBody as List<GetHistoryResponseItem>
                    val sortedList = detectDiseaseItems.sortedByDescending { it.detectionDate }
                    val detectAdapter = DetectDiseaseHistoryAdapter(
                        sortedList,
                        object : DetectDiseaseHistoryAdapter.OnAdapterClickListener {
                            override fun onItemClicked(detectDisease: GetHistoryResponseItem) {
                                val intent = Intent(this@MainActivity, HistoryResultDetectionActivity::class.java)
                                intent.putExtra("result_name", detectDisease.detectionResult)
                                intent.putExtra("result_image", detectDisease.imageUrl)
                                intent.putExtra("result_date", detectDisease.detectionDate)
                                startActivity(intent)
                            }
                        })
                    detectCounter.text = detectDiseaseItems.size.toString()

                    binding.rvDeteksi.apply {
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        adapter = detectAdapter
                    }
                }
            }

            override fun onFailure(call: Call<List<GetHistoryResponseItem>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Gagal mendapatkan data", Toast.LENGTH_SHORT).show()
            }

        })
    }

    // Exit app when back button pressed
    override fun onBackPressed() {
        finishAffinity()
    }
}