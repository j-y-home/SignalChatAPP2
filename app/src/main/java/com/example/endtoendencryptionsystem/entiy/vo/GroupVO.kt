package com.example.endtoendencryptionsystem.entiy.vo




data class GroupVO (
    val id: Long,
    val name: String,
    val ownerId: Long,
    val headImage: String = "",
    val headImageThumb: String = "",
    val notice: String? = null,
    val remarkNickName: String? = null,
    val showNickName: String? = null,
    val showGroupName: String? = null,
    val remarkGroupName: String? = null,
    val dissolve: Boolean? = null,
    val quit: Boolean? = null,
    val isBanned: Boolean? = null,
    val reason: String? = null
)
