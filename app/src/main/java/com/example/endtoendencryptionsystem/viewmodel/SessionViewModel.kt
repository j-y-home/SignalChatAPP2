package com.example.endtoendencryptionsystem.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.repository.ChatMsgRepository
import com.example.endtoendencryptionsystem.repository.SessionRepository
import com.example.endtoendencryptionsystem.utils.SignalKeyManager
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id

import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.launch

class SessionViewModel(app: Application): AutoDisposeViewModel(app) {

    private val sessionRepository: SessionRepository = SessionRepository()
    private val chatMsgRepository: ChatMsgRepository = ChatMsgRepository(app)
    var refreshResult = MutableLiveData<LoginVO>()

    fun createSession(username:String,password:String): Flowable<LoginVO>{
        Log.e("xxxx","createSession")
        return sessionRepository.createSession(username, password)
            .doOnNext { loginVO ->
                viewModelScope.launch {
                    //TODO 登录后的密钥注册逻辑
                  //  initOrRegisterSignalKeys(loginVO.userId.toString())
                    //TODO 登录成功后做以下操作
//                    syncFriendList(loginVO.userId.toString())
//                    syncGroupInfo(loginVO.userId.toString())
                }
            }
    }

    /**
     * 该方法之后要加载（刷新）自己、好友列表、群等信息（参考uniapp）
     * 先做自己信息的获取，其他的待 TODO
     */
    fun refreshToken(refreshToken: String):Flowable<LoginVO>{
        Log.e("xxxx","refreshToken")
       return sessionRepository.refreshToken(refreshToken)
    }

    fun getConversation():Flowable<List<ChatConversation>>{
        return chatMsgRepository.getAllConversations()
    }

    fun getMyInfo(){
        sessionRepository.getMyInfo()
    }

    /**
     * 初始化或注册 Signal 密钥
     */
    private suspend fun initOrRegisterSignalKeys(userId: String) {
        val result = SignalKeyManager.initOrRegisterSignalKeysIfNecessary(userId)

        result?.let {
            Log.i("Login", "新密钥已注册并上传")
            // 可选：上传到服务器
        } ?: run {
            Log.i("Login", "已有密钥，无需操作")
        }
    }

}