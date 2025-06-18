package com.example.endtoendencryptionsystem.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSONObject
import com.example.endtoendencryptionsystem.ETEApplication
import com.example.endtoendencryptionsystem.repository.KeyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 本地的密钥信息管理类（自己的密钥信息）
 */
object SignalKeyManager {

    private val keyRepository by lazy {
        KeyRepository(ETEApplication.getInstance()!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun initOrRegisterSignalKeysIfNecessary(userId: String): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 检查本地是否存在身份密钥
                val existingKey = keyRepository.getIdentityKey(userId)
                if (existingKey != null) {
                    // 已存在，无需注册
                    Log.d("SignalKeyManager", "已存在身份密钥，跳过注册")
                    return@withContext null
                }

                // 2. 不存在，注册新密钥
                Log.d("SignalKeyManager", "未找到身份密钥，开始注册")
                val registerResult = EncryptionUtil.registerKey()

                // 3. 可选：将 registerResult 上传到服务器
                if (registerResult != null) {
                    uploadKeysToServer(registerResult.toJSONString())
                }

                registerResult
            } catch (e: Exception) {
                Log.e("SignalKeyManager", "密钥初始化失败", e)
                null
            }
        }
    }

    private fun uploadKeysToServer(keyJson: String) {
        // TODO: 使用 Retrofit 或 OkHttp 将 keyJson 发送到服务器
        Log.i("SignalKeyManager", "密钥已上传到服务器: $keyJson")
    }
}
