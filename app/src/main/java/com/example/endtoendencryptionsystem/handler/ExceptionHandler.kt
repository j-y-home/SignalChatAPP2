package com.wumingtech.at.handler

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.endtoendencryptionsystem.handler.GlobalErrorTransformer
import com.example.endtoendencryptionsystem.http.response.AuthException
import com.example.endtoendencryptionsystem.http.response.BusinessException
import com.lnsoft.conslutationsystem.core.AppActivityManager
import com.ruins.library.sweet.SweetAlertDialog
import com.ruins.library.sweet.SweetAlertType

import java.net.SocketTimeoutException

fun <T : Any> handleGlobalError(context: Context) : GlobalErrorTransformer<T> = GlobalErrorTransformer(
    onErrorConsumer = {
        when(it) {
            is AuthException -> {
                //TODO
              //  TheRouter.build("/activity/login").withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation()
            }
            is SocketTimeoutException -> {
               // SweetAlertDialog(AppActivityManager.getCurrentActivity(),SweetAlertType.ERROR_TYPE).setContentText("网络连接超时，请检查网络或联系客服。").show()
            }
            is BusinessException -> {
                Toast.makeText(AppActivityManager.getCurrentActivity(),it.message,Toast.LENGTH_SHORT).show()
//                try {
//                    SweetAlertDialog(AppActivityManager.getCurrentActivity(),SweetAlertType.ERROR_TYPE).setContentText(it.message).show()
//                }catch (e: Exception){
//                    Log.e("错误详情", "怎么不显示呢"+e.message)
//                }

            }
            else -> {
                Log.e("错误详情", it.stackTraceToString())
            //    SweetAlertDialog(AppActivityManager.getCurrentActivity(),SweetAlertType.ERROR_TYPE).setContentText(it.message).show()
            }
        }
    }
)