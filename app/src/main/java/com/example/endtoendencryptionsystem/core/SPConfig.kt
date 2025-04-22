package com.lnsoft.conslutationsystem.core

import android.os.Environment
import com.example.endtoendencryptionsystem.ETEApplication
import com.tencent.mmkv.MMKV


object SPConfig {

 /**
  * 未读消息个数
  */
 const val UN_READ_MSG_NUM: String = "unreadMsgNum"
    /**
     * 未读好有请求个数
     */
    const val UN_READ_FRIEND_NUM: String = "unreadFriendNum"

    /**
     *
     */
    const val USER: String = "user"

    const val MSG_TYPE_IMAGE: String = "image"

    const val MSG_TYPE_LOCATION: String = "location"

    const val MSG_TYPE_VOICE: String = "voice"

    const val MSG_TYPE_CUSTOM: String = "custom"

    const val MSG_TYPE_SYSTEM: String = "eventNotification"


    const val TARGET_TYPE_SINGLE: String = "single"

    const val TARGET_TYPE_GROUP: String = "group"

    const val TARGET_TYPE_CHATROOM: String = "chatroom"


    const val DEFAULT_PAGE_SIZE: Int = 10

    // 创建群聊方式
    const val CREATE_GROUP_TYPE_FROM_NULL: String = "1"


    const val CREATE_GROUP_TYPE_FROM_SINGLE: String = "2"


    const val CREATE_GROUP_TYPE_FROM_GROUP: String = "3"


    // 好友来源
    /**
     * 来自手机号搜索
     */
    const val FRIENDS_SOURCE_BY_PHONE: String = "1"

    /**
     * 来自微信号搜索
     */
    const val FRIENDS_SOURCE_BY_WX_ID: String = "2"

    /**
     * 来自附近的人
     */
    const val FRIENDS_SOURCE_BY_PEOPLE_NEARBY: String = "3"

    /**
     * 来自手机通讯录
     */
    const val FRIENDS_SOURCE_BY_CONTACT: String = "4"

    /**
     * 来自手机号搜索
     */
    const val CONTACTS_FROM_PHONE: String = "1"

    /**
     * 来自微信号搜索
     */
    const val CONTACTS_FROM_WX_ID: String = "2"

    /**
     * 来自附近的人
     */
    const val CONTACTS_FROM_PEOPLE_NEARBY: String = "3"

    /**
     * 来自手机通讯录
     */
    const val CONTACTS_FROM_CONTACT: String = "4"

    /**
     * 朋友权限（所有权限：聊天、朋友圈、微信运动等）
     */
    const val PRIVACY_CHATS_MOMENTS_WERUN_ETC: String = "0"

    /**
     * 朋友权限（仅聊天）
     */
    const val PRIVACY_CHATS_ONLY: String = "1"

    /**
     * 朋友圈和视频动态-可以看我
     */
    const val SHOW_MY_POSTS: String = "0"

    /**
     * 朋友圈时视频动态-不让他看我
     */
    const val HIDE_MY_POSTS: String = "1"

    /**
     * 朋友圈和视频动态-可以看他
     */
    const val SHOW_HIS_POSTS: String = "0"

    /**
     * 朋友圈时视频动态-不看他
     */
    const val HIDE_HIS_POSTS: String = "1"

    /**
     * 非星标好友
     */
    const val CONTACT_IS_NOT_STARRED: String = "0"

    /**
     * 星标好友
     */
    const val CONTACT_IS_STARRED: String = "1"

    /**
     * 非黑名单
     */
    const val CONTACT_IS_NOT_BLOCKED: String = "0"

    /**
     * 黑名单
     */
    const val CONTACT_IS_BLOCKED: String = "1"

    /**
     * 星标好友分组title
     */
    const val STAR_FRIEND: String = "星标朋友"

    /**
     * 用户微信号修改标记
     */
    const val USER_WX_ID_MODIFY_FLAG_TRUE: String = "1"

    /**
     * 地区类型-"省"
     */
    const val AREA_TYPE_PROVINCE: String = "1"

    /**
     * 地区类型-"市"
     */
    const val AREA_TYPE_CITY: String = "2"

    /**
     * 地区类型-"县"
     */
    const val AREA_TYPE_DISTRICT: String = "3"

    /**
     * 定位类型-地区信息
     * 获取省市区街道信息
     */
    const val LOCATION_TYPE_AREA: String = "0"

    /**
     * 定位类型-消息
     * 发送定位信息
     */
    const val LOCATION_TYPE_MSG: String = "1"

    const val DEFAULT_POST_CODE: String = "000000"


    // 登录方式
    /**
     * 手机号/密码登录
     */
    const val LOGIN_TYPE_PHONE_AND_PASSWORD: String = "0"

    /**
     * 手机号/验证码登录
     */
    const val LOGIN_TYPE_PHONE_AND_VERIFICATION_CODE: String = "1"

    /**
     * 微信号/QQ/邮箱登录
     */
    const val LOGIN_TYPE_OTHER_ACCOUNTS_AND_PASSWORD: String = "2"

    /**
     * 验证码业务类型-"登录"
     */
    const val VERIFICATION_CODE_SERVICE_TYPE_LOGIN: String = "0"


    // QQ号验证
    /**
     * 未绑定
     */
    const val QQ_ID_NOT_LINK: String = "0"

    /**
     * 已绑定
     */
    const val QQ_ID_LINKED: String = "1"


    // 邮箱验证
    /**
     * 未绑定
     */
    const val EMAIL_NOT_LINK: String = "0"

    /**
     * 未验证
     */
    const val EMAIL_NOT_VERIFIED: String = "1"

    /**
     * 已验证
     */
    const val EMAIL_VERIFIED: String = "2"

    /**
     * 热词阈值
     */
    const val HOT_SEARCH_THRESHOLD: Int = 8

    /**
     * 普通注册用户
     */
    const val USER_TYPE_REG: String = "REG"

    /**
     * 微信团队
     */
    const val USER_TYPE_WEIXIN: String = "WEIXIN"

    /**
     * 文件传输助手
     */
    const val USER_TYPE_FILEHELPER: String = "FILEHELPER"


    // SharedPreferences key
    /**
     * 已选标签
     */
    const val SP_KEY_TAG_SELECTED: String = "tag_selected"
}