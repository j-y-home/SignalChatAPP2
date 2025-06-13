package com.example.endtoendencryptionsystem.http.response

data class ApiResponse<T>(val code:Int,val message:String,val data:T?)
