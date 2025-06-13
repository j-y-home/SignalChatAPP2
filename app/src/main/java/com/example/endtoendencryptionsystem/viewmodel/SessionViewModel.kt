package com.example.endtoendencryptionsystem.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import autodispose2.autoDispose
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.repository.SessionRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

class SessionViewModel(app: Application): AutoDisposeViewModel(app) {

    private val sessionRepository: SessionRepository = SessionRepository()
    var refreshResult = MutableLiveData<LoginVO>()

    fun createSession(username:String,password:String): Flowable<LoginVO>{
        Log.e("xxxx","createSession")
        return sessionRepository.createSession(username, password)
    }

    fun refreshToken(refreshToken: String){
        Log.e("xxxx","refreshToken")
        sessionRepository.refreshToken(refreshToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                refreshResult.postValue(it)
            }, {})
    }

}