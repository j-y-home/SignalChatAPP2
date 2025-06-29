package com.example.endtoendencryptionsystem.http

import com.fasterxml.jackson.databind.ObjectWriter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Converter
import java.io.IOException

internal class JacksonRequestBodyConverter<T>(private val adapter:ObjectWriter) : Converter<T, RequestBody> {

    @Throws(IOException::class)
    override fun convert(value: T): RequestBody {
        val bytes: ByteArray = adapter.writeValueAsBytes(value)
        return bytes.toRequestBody("application/json; charset=UTF-8".toMediaType())
    }
}