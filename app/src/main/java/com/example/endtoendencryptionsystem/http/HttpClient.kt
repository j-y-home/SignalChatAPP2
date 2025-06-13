package com.example.endtoendencryptionsystem.http

import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.util.concurrent.TimeUnit

/**
 * HttpClient
 * @version 1.1.0
 * @author jihaopeng
 * @constructor 创建 HttpClient 实例,Create HttpClient instance
 * @param baseUrl 基础 URL,Base URL
 * @param interceptors 拦截器,Interceptor
 * @param networkInterceptors 网络拦截器,Network Interceptor
 * @param config OkHttp 配置,OkHttp configuration
 * @param converterFactory 转换器工厂,Converter factory
 * @param callAdapterType 调用适配器类型,Call adapter type
 * @param authenticator 认证器,Authenticator
 */
class HttpClient(
    baseUrl: String,
    interceptors: List<Interceptor>,
    networkInterceptors: List<Interceptor>,
    config: OkHttpConfig,
    converterFactory:Converter.Factory,
    callAdapterType: CallAdapterType = CallAdapterType.RXJAVA3,
    authenticator: Authenticator? = null
) {
    private val retrofit: Retrofit

    init {
        val builder = OkHttpClient.Builder()
        interceptors.forEach {
            builder.addInterceptor(it)
        }
        networkInterceptors.forEach {
            builder.addNetworkInterceptor(it)
        }
        authenticator?.let {
            builder.authenticator(it)
        }
        val client = builder.connectTimeout(config.connectTimeout, TimeUnit.SECONDS)
            .readTimeout(config.readTimeout, TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeout, TimeUnit.SECONDS)
            .retryOnConnectionFailure(config.retryOnConnectionFailure)
            .build()
        val callAdapter = if (callAdapterType == CallAdapterType.RXJAVA3) RxJava3CallAdapterFactory.create()
                          else SuspendResultCallAdapterFactory()

        retrofit = Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapter).build()
    }

    /**
     * 创建 Retrofit 服务实例
     *
     * @param tClass 服务接口的 Class 对象
     * @param <T> 服务接口的类型
     * @return Retrofit 服务实例
     */
    fun <T> createService(tClass: Class<T>): T {
        return retrofit.create(tClass)
    }
}