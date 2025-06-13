package com.example.endtoendencryptionsystem.repository

import android.app.Application
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.example.endtoendencryptionsystem.entiy.FriendItem
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.ChatMetadata
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.Group
import com.example.endtoendencryptionsystem.entiy.database.GroupChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage
import com.example.endtoendencryptionsystem.entiy.database.User
import com.example.endtoendencryptionsystem.enums.MessageStatus
import com.example.endtoendencryptionsystem.utils.PinyinUtils
import com.example.endtoendencryptionsystem.utils.isOnline

import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toFriend
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.utils.toObject
import com.tencent.mmkv.MMKV
import com.wumingtech.at.http.ApiFactory
import io.dcloud.p.f
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import org.reactivestreams.Publisher
import org.whispersystems.libsignal.util.ByteUtil
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set


class FriendRepository(val app: Application) {

    private var db = AppDatabase.getDatabase(app)
    private val friendsDao = db.friendDao()
    private val TAG: String = "FriendRepository"

    /**
     * 获取当前用户的所有好友
     * id:当前用户的id
     */
    fun selectAllFriendsByUserId(userId: Int): Flowable<List<Friend>> {
        return Flowable.create({ emitter ->
            emitter.onNext(friendsDao.selectAllFriendsByUserId(userId))
            emitter.onComplete()
        }, BackpressureStrategy.ERROR)
    }

    /**
     * 获取某个好友的详细信息
     * id:好友id
     */
    fun selectFriendsByFriendId(friendId: Long): Friend {
        return friendsDao.selectFriendsByFriendId(friendId);
    }



    fun selectAllData():List<Friend>{
        return friendsDao.selectAllData()
    }


    /**
     * 获取用户的好友列表：按首字母排序并分组
     */
    fun generateGroupedList():  Flowable<List<FriendItem>> {
        return Flowable.create({ emitter ->
            //从数据库获取好友数据
            val friends = friendsDao.selectAllFriendsByUserId(MMKV.defaultMMKV().decodeInt("userId"))
            // 先提取每个好友的拼音首字母，并缓存用于排序和分组
            val friendsWithInitial = friends.map { friend ->
                val initial = getFirstLetter(friend.friendNickName.toString())
                Log.e("xxxx","initial:"+initial)
                Pair(initial, friend)
            }
            // 按拼音首字母排序
            val sortedList = friendsWithInitial.sortedBy { it.first }.map { it.second }
            // 按拼音首字母分组
            val groupedMap = sortedList.groupBy { getFirstLetter(it.friendNickName.toString()) }
            val result = mutableListOf<FriendItem>()
            for ((letter, groupFriends) in groupedMap) {
                Log.e("xxxx","letter:"+letter)
                result.add(FriendItem.Header(letter))
                result.addAll(groupFriends.map { FriendItem.FriendEntry(it) })
            }

            emitter.onNext(result)
            emitter.onComplete()
        }, BackpressureStrategy.ERROR)
    }

    private fun getFirstLetter(name: String): String {
        return PinyinUtils.getPinyinInitials(name)
    }


    /**
     * 从服务器获取好友列表
     */
    fun getFriends(): Flowable<Boolean>{
        return if (app.isOnline()) {
            Log.e("xxx","这儿1")
            ApiFactory.API.api.getFriends()
                .flatMap{ its->
                    Log.e("xxx","同步的好友列表："+json.toJSONString(its))
                    its.forEach {
                        friendsDao.addFriend(it.toFriend())
                    }
                    return@flatMap Flowable.just(true)
                }
        } else {
            Flowable.create({
                it.onError(Throwable("请在网络良好的条件下同步好友列表"))
            }, BackpressureStrategy.ERROR)
        }
    }

    /**
     * 根据用户名或昵称搜索用户
     */
    fun getSearchUsersByKey(key: String): Flowable<List<User>> {
        return if (app.isOnline()) {
            Log.e("xxx","这儿1")
            ApiFactory.API.api.getSearchUsersByKey(key)
                .flatMap{ users->
                    val localFriends = friendsDao.selectAllFriendsByUserId(MMKV.defaultMMKV().decodeInt("userId"))
                    if(localFriends.isNotEmpty()){
                        users.forEach { user ->
                            val friend = localFriends.find { it.friendId == user.id }
                            if (friend != null) {
                                user.isFriend = true
                            }
                        }
                    }
                    Log.e("xxx","这儿2")
                    return@flatMap Flowable.just(users)
                }
        } else {
            Flowable.create({
                it.onError(Throwable("请在网络良好的条件下搜索用户"))
            }, BackpressureStrategy.ERROR)
        }
    }


    /**
     * 添加好友
     */
    fun addFriend(friend: Friend) : Flowable<Boolean>{
        return if (app.isOnline()) {
            ApiFactory.API.api.addFriend(friend.friendId)
                .flatMap{ result->
                    friendsDao.addFriend(friend)
                    return@flatMap Flowable.just(true)
                }
        } else {
            Flowable.create({
                it.onError(Throwable("请在网络良好的条件下添加好友"))
            }, BackpressureStrategy.ERROR)
        }

    }

    /**
     * 删除好友
     */
    fun delFriend(friendId: Long) : Flowable<Boolean>{
        return if (app.isOnline()) {
            ApiFactory.API.api.delFriend(friendId)
                .flatMap{ result->
                    friendsDao.deleteFriendById(friendId)
                    return@flatMap Flowable.just(true)
                }
        } else {
            Flowable.create({
                it.onError(Throwable("请在网络良好的条件下删除好友"))
            }, BackpressureStrategy.ERROR)
        }

    }
}