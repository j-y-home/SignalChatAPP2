package com.example.endtoendencryptionsystem.entiy.dao

import androidx.room.*
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage


@Dao
interface PrivateMessageDao {

    /**
     * 获取与某个好友的所有聊天记录
     * userId:当前用户的id
     * friendId:好友的id
     */
    @Query("select * from im_private_message where send_id=:userId and recv_id =:friendId")
    fun getAllMsgFromFriend(userId: Int,friendId: Int): List<PrivateMessage>


    /**
     * 新增一条私聊消息
     */
    @Insert
    fun insertMessage(privateMessage: PrivateMessage)


}