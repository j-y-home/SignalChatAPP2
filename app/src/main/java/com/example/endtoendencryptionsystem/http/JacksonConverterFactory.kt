package com.example.endtoendencryptionsystem.http

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object JacksonConverterFactory : Converter.Factory() {
    private val mapper = ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).registerKotlinModule()

    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<ResponseBody,Any> {
        val javaType = mapper.typeFactory.constructType(type)
        val reader: ObjectReader = mapper.readerFor(javaType)
        return JacksonResponseBodyConverter(mapper,reader)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        val javaType: JavaType = mapper.typeFactory.constructType(type)
        val writer: ObjectWriter = mapper.writerFor(javaType)
        return JacksonRequestBodyConverter<Any>(writer)
    }
}