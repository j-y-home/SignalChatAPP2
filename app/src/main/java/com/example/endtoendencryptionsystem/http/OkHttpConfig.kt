package com.example.endtoendencryptionsystem.http

/**
 * Created by wumingtech on 2021/9/26.
 * Description:OkHttp 配置
 */
data class OkHttpConfig(
    val connectTimeout: Long,
    val readTimeout: Long,
    val writeTimeout: Long,
    val retryOnConnectionFailure: Boolean = true
)
