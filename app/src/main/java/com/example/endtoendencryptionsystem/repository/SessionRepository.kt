package com.example.endtoendencryptionsystem.repository

import com.example.endtoendencryptionsystem.entiy.dto.LoginDTO
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.wumingtech.at.http.ApiFactory
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

open class SessionRepository() {

    fun createSession(username: String, password: String): Flowable<LoginVO> {
        return ApiFactory.API.api.login(LoginDTO(username, password, 1))
    }

    fun refreshToken(refreshToken: String): Flowable<LoginVO> {
        return ApiFactory.API.api.refreshToken(refreshToken)
    }
}