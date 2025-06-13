package com.example.endtoendencryptionsystem.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import autodispose2.autoDispose
import com.example.endtoendencryptionsystem.entiy.FriendItem
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.User

import com.example.endtoendencryptionsystem.repository.ChatRepository
import com.example.endtoendencryptionsystem.repository.FriendRepository
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class FriendViewModel(app: Application) : AutoDisposeViewModel(app) {
    private val friendRepository = FriendRepository(app)
    var friendList = MutableLiveData<List<FriendItem>>()
    var userFriendsList = MutableLiveData<List<Friend>>()
    var searchUsersList = MutableLiveData<List<User>>()
    var addFriendResult = MutableLiveData<Boolean>()
    var delFriendResult = MutableLiveData<Boolean>()
    var syncFriendResult = MutableLiveData<Boolean>()
    /**
     * 根据用户名或昵称搜索用户
     */
    fun getFriends() {
        Log.e("xxx","这儿4")
        friendRepository.getFriends()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                syncFriendResult.postValue(it)
            }, {})
    }
    /**
     * 从服务器同步自己的好友列表
     */
    fun syncFriendList() {

    }

    /**
     * 根据用户名或昵称搜索用户
     */
    fun getSearchUserListByKey(key: String) {
        Log.e("xxx","这儿4")
        friendRepository.getSearchUsersByKey(key)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                Log.e("xxxx","这儿")
                searchUsersList.postValue(it)
            }, {})
    }

    /**
     * 添加好友
     */
    fun addFriend(friend: Friend) {
        friendRepository.addFriend(friend)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                addFriendResult.postValue(it)
            }, {})
    }

    /**
     * 删除好友
     */
    fun delFriend(friendId: Long) {
        friendRepository.delFriend(friendId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                delFriendResult.postValue(it)
            }, {})
    }

    /**
     * 获取分组后的好友列表
     */
    fun getGroupedFriendList() {
        friendRepository.generateGroupedList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                Log.e("xxxx","分组后："+it.size+ json.toJSONString(it))
                friendList.postValue(it)
            }, {})
    }

    /**
     * 获取用户的好友列表
     */
    fun getUserFriendList(userId: Int) {
        friendRepository.selectAllFriendsByUserId(userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                userFriendsList.postValue(it)
            }, {})
    }


}