package com.example.endtoendencryptionsystem.http

import android.util.Log
import com.example.endtoendencryptionsystem.http.response.AuthException
import com.example.endtoendencryptionsystem.http.response.BusinessException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException

internal class JacksonResponseBodyConverter(
    private val mapper: ObjectMapper,
    private val adapter: ObjectReader
) : Converter<ResponseBody, Any> {

    @Throws(BusinessException::class,IOException::class)
    override fun convert(value: ResponseBody): Any {
        value.use {
            // ResponseBody 只能读取一次，如需再次使用需用变量保存
            val resString = value.string()
            val resTree = mapper.readTree(resString)
            val successCode = 200
            when {
                resTree["code"].asInt() == successCode -> {
                    if (resTree["data"].isNull) {
                        return true
                    }
                    return adapter.readValue(resTree["data"])
                }
                resTree["code"].asInt() == 500 -> {
                    throw BusinessException(resTree["message"].asText())
                }
                else -> {
                    throw BusinessException(resTree["message"].asText())
                }
            }
        }
    }
}