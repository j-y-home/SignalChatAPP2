package com.lnsoft.conslutationsystem.core

import android.os.Environment
import com.example.endtoendencryptionsystem.ETEApplication
import com.tencent.mmkv.MMKV


object Config {
    private var ip = MMKV.defaultMMKV().decodeString("IP","")
    private var dk = MMKV.defaultMMKV().decodeString("DK","")
    //var BaseURL = "https://328eb748ng14.vicp.fun/"
   var BaseURL = if(ip!!.isNotEmpty()&& dk!!.isNotEmpty()){
        "https://$ip:$dk/"
    }else if(ip!!.isNotEmpty()){
        "https://$ip/"
    }else{
        "https://328eb748ng14.vicp.fun/"
    }

    var dbFilePath2 =
        "//data//data//" + ETEApplication.getInstance()?.packageName + "//databases//"
    var dbFilePath =  Environment.getExternalStorageDirectory().absolutePath + "/病情/数据库/database/"
    var dbFile =  Environment.getExternalStorageDirectory().absolutePath + "/病情/数据库/database/Co.db"
    var zipFilePath =
        Environment.getExternalStorageDirectory().absolutePath + "/病情/数据库"
    var zipFilePath2 =
        "//data//data//" + ETEApplication.getInstance()?.packageName
    var CaseFilePath =
        Environment.getExternalStorageDirectory().absolutePath + "/病情/病例图片"

    const val originalMedicationJson =
        "[{\"itemType\":6,\"text\":\"甲钴胺\",\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.025mg\"},{\"text\":\"0.05mg\"},{\"text\":\"0.1mg\"},{\"text\":\"0.25mg\"},{\"text\":\"0.5mg\"}]},{\"itemType\":6,\"text\":\"曲安奈德\",\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.1mg\"},{\"text\":\"0.2mg\"},{\"text\":\"1mg\"},{\"text\":\"2mg\"},{\"text\":\"10mg\"}]},{\"itemType\":6,\"text\":\"利多卡因\",\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.4mg\"},{\"text\":\"1mg\"},{\"text\":\"1.5mg\"},{\"text\":\"2mg\"},{\"text\":\"4mg\"},{\"text\":\"5mg\"},{\"text\":\"10mg\"},{\"text\":\"100mg\"}]},{\"itemType\":6,\"text\":\"山莨菪碱\",\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.02mg\"},{\"text\":\"1mg\"},{\"text\":\"2mg\"},{\"text\":\"5mg\"}]},{\"itemType\":6,\"text\":\"地塞米松\",\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.2mg\"},{\"text\":\"1mg\"},{\"text\":\"2mg\"},{\"text\":\"5mg\"}]},{\"itemType\":6,\"text\":\"舒血宁\",\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.1ml\"},{\"text\":\"0.2ml\"},{\"text\":\"0.5ml\"},{\"text\":\"1ml\"}]},{\"itemType\":6,\"text\":\"生理盐水\",\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.1ml\"},{\"text\":\"1ml\"},{\"text\":\"5ml\"},{\"text\":\"15ml\"}]},{\"itemType\":6,\"text\":\"黄芪注射液\",\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.1ml\"},{\"text\":\"1ml\"},{\"text\":\"5ml\"},{\"text\":\"15ml\"}]}]"
    const val instrumentJson =
        "{\"itemType\":2,\"value\":\"\",\"field\":\"zz\",\"group\":1,\"childItem\":[{\"text\":\"0.3*25mm\"},{\"text\":\"0.5*35mm\"},{\"text\":\"0.5*60mm\"}]}"

    var voiceFilePath =
        ETEApplication.getInstance()!!.getExternalFilesDir("")!!.absolutePath + "/voice"

    const val MIN_VOICE_TIME = 3

    var medicationJson = if(MMKV.defaultMMKV().decodeString("medication").isNullOrEmpty()){ originalMedicationJson}else{ MMKV.defaultMMKV().decodeString("medication") }




    const val USER_SEX_MALE: String = "1"

    const val USER_SEX_FEMALE: String = "2"

    const val IS_NOT_FRIEND: String = "0"

    const val IS_FRIEND: String = "1"

    const val FRIEND_APPLY_STATUS_ACCEPT: String = "1"


    // 用于推送的业务类型
    /**
     * 好友申请
     */

    const val PUSH_SERVICE_TYPE_ADD_FRIENDS_APPLY: String = "ADD_FRIENDS_APPLY"

    /**
     * 好友接收
     */

    const val PUSH_SERVICE_TYPE_ADD_FRIENDS_ACCEPT: String = "ADD_FRIENDS_ACCEPT"



    const val MSG_TYPE_TEXT: String = "text"

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