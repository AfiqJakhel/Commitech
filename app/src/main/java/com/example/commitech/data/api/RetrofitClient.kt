package com.example.commitech.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // Ganti dengan IP address komputer Anda untuk testing di emulator
    // Untuk emulator Android: gunakan 10.0.2.2
    // Untuk device fisik: gunakan IP address komputer Anda (misal: 192.168.1.100)
    private const val BASE_URL = "http://10.0.2.2:8000/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Configure Gson to be lenient for malformed JSON
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Connection timeout: waktu maksimal untuk establish connection
        .connectTimeout(15, TimeUnit.SECONDS)
        // Read timeout: waktu maksimal untuk membaca response (510ms response time + buffer)
        .readTimeout(60, TimeUnit.SECONDS)
        // Write timeout: waktu maksimal untuk write request
        .writeTimeout(30, TimeUnit.SECONDS)
        // Call timeout: total waktu maksimal untuk satu request (lebih dari read timeout)
        .callTimeout(90, TimeUnit.SECONDS)
        // Connection pooling untuk reuse connection
        .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
        // Retry on connection failure
        .retryOnConnectionFailure(true)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
