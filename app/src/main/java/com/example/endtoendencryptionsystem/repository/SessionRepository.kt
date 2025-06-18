package com.example.endtoendencryptionsystem.repository

import com.example.endtoendencryptionsystem.entiy.dto.LoginDTO
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.entiy.vo.UserVO
import com.wumingtech.at.http.ApiFactory
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.Callable

open class SessionRepository() {

    fun createSession(username: String, password: String): Flowable<LoginVO> {
        return ApiFactory.API.api.login(LoginDTO(username, password, 1))
    }

    fun refreshToken(refreshToken: String): Flowable<LoginVO> {
        return ApiFactory.API.api.refreshToken(refreshToken)
    }

    fun getMyInfo(): Flowable<UserVO> {
        return ApiFactory.API.api.getMyInfo()
        return if (app.isOnline()) {
            ApiFactory.API.api.getTasks()
                .flatMap {
                    db.runInTransaction(Callable {
                        val deleteTaskList = metadataDao.getNotTSTask()
                        metadataDao.deleteAllTask(deleteTaskList)
                        metadataDao.insertTask(it)
                        metadataDao.getToDoGroupTask()
                    })
                }
        } else {
            metadataDao.getToDoGroupTask()
        }
    }
}