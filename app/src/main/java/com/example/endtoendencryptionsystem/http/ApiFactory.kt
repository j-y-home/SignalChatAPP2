package com.wumingtech.at.http


import com.example.endtoendencryptionsystem.http.Config
import com.example.endtoendencryptionsystem.http.HttpClient
import com.example.endtoendencryptionsystem.http.Interceptors
import com.example.endtoendencryptionsystem.http.Interceptors.defaultInterceptors
import com.example.endtoendencryptionsystem.http.JacksonConverterFactory
import com.example.endtoendencryptionsystem.http.OkHttpConfig



enum class ApiFactory(val api:Api) {
    API(
        HttpClient(
            Config.BaseURL, defaultInterceptors,
            Interceptors.defaultNetworkInterceptors,
            OkHttpConfig(10, 10, 10),
            JacksonConverterFactory
        ).createService(Api::class.java)
    )
}