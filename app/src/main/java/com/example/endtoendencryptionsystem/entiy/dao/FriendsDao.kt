package com.example.endtoendencryptionsystem.entiy.dao

import androidx.room.*
import com.example.endtoendencryptionsystem.entiy.database.Friend


@Dao
interface FriendsDao {

    /**
     * 获取当前用户的所有好友
     * id:当前用户的id
     */
    @Query("select * from im_friend where user_id=:id")
    fun selectAllFriendsByUserId(id: Int): List<Friend>

    /**
     * 查询某个好友的具体信息
     * id:当前用户的id
     */
    @Query("select * from im_friend where friend_id=:id")
    fun selectFriendsByFriendId(id: Long): Friend

    /**
     * 添加好友
     * 添加好友的时机：现在Uniapp做的是点击了“加好友”按钮，就互相为好友了（friends表同时新增两条数据）
     * 那在Android的数据库里如何确保好友的设备数据库里也能多一条数据（加我为好友，他的friend表多一条我的信息）呢？
     * 在点进会话的时候，操作，调用Android的这个方法，（onConflict = OnConflictStrategy.REPLACE）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFriend(friend: Friend)

    @Query("select * from im_friend")
    fun selectAllData():List<Friend>

    /**
     * 清空好友表
     */
    @Query("DELETE FROM im_friend")
    fun deleteAllFriends()
}