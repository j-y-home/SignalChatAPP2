package com.wumingtech.at.http


import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.User
import com.example.endtoendencryptionsystem.entiy.dto.GroupInviteDTO
import com.example.endtoendencryptionsystem.entiy.dto.GroupMessageDTO
import com.example.endtoendencryptionsystem.entiy.dto.LoginDTO
import com.example.endtoendencryptionsystem.entiy.dto.PrivateMessageDTO
import com.example.endtoendencryptionsystem.entiy.vo.FriendVO
import com.example.endtoendencryptionsystem.entiy.vo.GroupMemberRemoveVO
import com.example.endtoendencryptionsystem.entiy.vo.GroupMemberVO
import com.example.endtoendencryptionsystem.entiy.vo.GroupMessageVO
import com.example.endtoendencryptionsystem.entiy.vo.GroupVO
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.entiy.vo.PrivateMessageVO

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import retrofit2.http.*
import java.util.Objects

interface Api {

    /*-----------------------登录相关接口-------------------*/
    /**
     * 登录
     */
    @POST(value = "login")
    fun login(@Body body: LoginDTO): Flowable<LoginVO>

    /**
     * 刷新token
     */
    @PUT(value = "refreshToken")
    fun refreshToken(@Header(value = "refreshToken") refreshToken : String): Flowable<LoginVO>



    /*-----------------------好友相关接口-------------------*/

    /**
     * 获取好友列表
     */
    @GET(value = "friend/list")
    fun getFriends(): Flowable<List<FriendVO>>


    /**
     * 用户名或昵称搜索好友
     */
    @GET(value = "user/findByName")
    fun getSearchUsersByKey(@Query(value = "name") name: String?): Flowable<List<User>>

    /**
     * 添加好友
     */
    @POST(value = "friend/add")
    fun addFriend(@Query(value = "friendId") name: Long): Flowable<Boolean>

    /**
     * 删除好友
     */
    @DELETE(value = "friend/delete")
    fun delFriend(@Query(value = "friendId") name: Long): Flowable<Boolean>

    /*-----------------------私聊相关接口-------------------*/

    /**
     * 发送消息
     */
    @POST(value = "message/private/send")
    fun sendPrivateMsg(@Body body: PrivateMessageDTO): Flowable<PrivateMessageVO>

    /**
     * 撤回消息
     */
    @DELETE(value = "message/private/recall")
    fun recallPrivateMsg(@Query(value = "id") id: Long): Flowable<PrivateMessageVO>

    /**
     * 消息已读
     */
    @PUT(value = "message/private/readed")
    fun readPrivateMsg(@Query(value = "friendId") friendId: Long): Flowable<Boolean>

    /*-----------------------群组相关接口-------------------*/

    /**
     * 创建群聊
     */
    @POST(value = "group/create")
    fun createGroup(@Body body: GroupVO): Flowable<GroupVO>

    /**
     * 修改群聊信息
     */
    @PUT(value = "group/modifyGroup")
    fun modifyGroup(@Body body: GroupVO): Flowable<GroupVO>

    /**
     * 解散群聊
     */
    @DELETE(value = "group/delete")
    fun deleteGroup(@Query(value = "groupId") groupId: Long): Flowable<Boolean>

    /**
     * 获取群聊列表
     */
    @GET(value = "group/list")
    fun getGroups(): Flowable<List<GroupVO>>

    /**
     * 邀请进群
     */
    @POST(value = "group/invite")
    fun inviteGroup(@Body body:GroupInviteDTO): Flowable<Boolean>

    /**
     * 查询群聊成员
     */
    @GET(value = "group/members")
    fun getGroupMembers(@Query(value = "groupId") groupId: Long): Flowable<List<GroupMemberVO>>

    /**
     * 将成员移出群聊
     */
    @DELETE(value = "group/members/remove")
    fun removeGroupMember(@Body body: GroupMemberRemoveVO): Flowable<Boolean>

    /**
     * 退出群聊
     */
    @DELETE(value = "group/quit")
    fun quitGroup(@Query(value = "groupId") groupId: Long): Flowable<Boolean>


    /*-----------------------群聊相关接口-------------------*/


    /**
     * 发送消息
     */
    @POST(value = "message/group/send")
    fun sendGroupMsg(@Body body: GroupMessageDTO): Flowable<GroupMessageVO>

    /**
     * 撤回消息 ：TODO :由id都改为messageId，因为后期不保存在数据库就没有这个id了
     */
    @DELETE(value = "message/group/recall")
    fun recallGroupMsg(@Query(value = "id") id: Long): Flowable<GroupMessageVO>
























}