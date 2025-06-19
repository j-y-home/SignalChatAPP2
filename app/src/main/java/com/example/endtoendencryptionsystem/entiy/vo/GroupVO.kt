package com.example.endtoendencryptionsystem.entiy.vo




data class GroupVO (
    val id: Long?=0,
    val name: String?="",
    val ownerId: Long? = 0,
    val headImage: String? = "",
    val headImageThumb: String? = "",
    val notice: String? = "",
    val remarkNickName: String? = "",
    val showNickName: String? = "",
    val showGroupName: String? = "",
    val remarkGroupName: String? = "",
    val dissolve: Boolean? = false,
    val quit: Boolean? = false,
    val isBanned: Boolean? = false,
    val reason: String? = ""
)
