package com.example.endtoendencryptionsystem.enums

enum class MessageStatus(val code: Int,val value:String) {
    /**
     * 文件
     */
    UNSEND(0, "未送达"),

    /**
     * 文件
     */
    SENDED(1, "送达"),

    /**
     * 撤回
     */
    RECALL(2, "撤回"),

    /**
     * 已读
     */
    READED(3, "已读")

}
