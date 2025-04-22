//package com.example.endtoendencryptionsystem.utils
//
//import com.tencent.mmkv.MMKV
//import okhttp3.Interceptor
//import okhttp3.Response
//import okhttp3.logging.HttpLoggingInterceptor
//
//object Interceptors {
//    private var isShowLog: Boolean = false
//    fun getInterceptors(): ArrayList<Interceptor> {
//        val interceptors = ArrayList<Interceptor>()
//        if (isShowLog) {
//            // 日志拦截器
//            val logInterceptor = HttpLoggingInterceptor()
//            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
//            interceptors.add(logInterceptor)
//        }
//        // 网络拦截器
//        val networkInterceptor = NetworkInterceptor()
//        interceptors.add(networkInterceptor)
//        return interceptors
//    }
//
//    @Synchronized
//    fun openLog() {
//        isShowLog = true
//    }
//
//    class NetworkInterceptor : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val request = chain.request().newBuilder()
//                .addHeader("Authorization", MMKV.defaultMMKV().decodeString("token") ?: "")
//                .build()
//            return chain.proceed(request)
//        }
//    }
//}