package com.example.endtoendencryptionsystem.http

import com.tencent.mmkv.MMKV
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/**
 * OkHttp 拦截器
 * OkHttp Interceptor
 */
object Interceptors {
    private var isShowLog: Boolean = false
    // 是否使用默认 token 拦截器
    private var useDefaultTokenInterceptor: Boolean = true

    val defaultInterceptors: ArrayList<Interceptor>
        get() {
            val interceptors = ArrayList<Interceptor>()
            // 添加默认 token 拦截器
            if (useDefaultTokenInterceptor){
                interceptors.add(defaultTokenInterceptor)
            }
            return interceptors
        }

    val defaultNetworkInterceptors: ArrayList<Interceptor>
        get() {
            val interceptors = ArrayList<Interceptor>()
            if (isShowLog) {
                interceptors.add(defaultLoggingInterceptor)
            }
            return interceptors
        }

    @Deprecated("use getInterceptors instead", ReplaceWith("defaultInterceptors"))
    fun getInterceptors(): ArrayList<Interceptor> {
        val interceptors = ArrayList<Interceptor>()
        if (isShowLog) {
            interceptors.add(defaultLoggingInterceptor)
        }
        // 添加默认 token 拦截器
        if (useDefaultTokenInterceptor){
            interceptors.add(defaultTokenInterceptor)
        }
        return interceptors
    }

    /**
     * 开启网络请求日志
     * Open network request log.
     */
    @Synchronized
    fun openLog() {
        isShowLog = true
    }

    /**
     * 是否使用默认 token 拦截器, 默认 false。
     * Whether to use the default token interceptor, default false.
     * @param use 是否使用默认 token 拦截器, 默认 false。 Whether to use the default token interceptor, default false.
     */
    @Synchronized
    fun useDefaultTokenInterceptor(use: Boolean=false) {
        useDefaultTokenInterceptor = use
    }

    private val defaultLoggingInterceptor: HttpLoggingInterceptor
        get() {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            return loggingInterceptor
        }

    private val defaultTokenInterceptor: Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("accessToken", MMKV.defaultMMKV().decodeString("accessToken") ?: "")
            .build()
        chain.proceed(request)
    }
}