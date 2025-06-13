package com.example.endtoendencryptionsystem.http.response

class BusinessException(override val message: String, val code: Int) : Exception(message)

/**
 * 验证(token)异常
 */
class AuthException(override var message: String) : Exception(message)
