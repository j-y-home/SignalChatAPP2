//package com.example.endtoendencryptionsystem.utils
//
//import com.fasterxml.jackson.databind.DeserializationFeature
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
//
///**
// * jackson 工具函数，使其 api 更加人性化
// */
//
//val json: ObjectMapper = jacksonObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//
//fun ObjectMapper.toJSONString(value: Any): String = this.writeValueAsString(value)
//
//inline fun <reified T> ObjectMapper.toObject(content: String): T = readValue(content, jacksonTypeRef<T>())