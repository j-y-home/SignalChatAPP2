package com.example.endtoendencryptionsystem.core

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.endtoendencryptionsystem.widget.AlertDialog
import com.example.endtoendencryptionsystem.widget.NoTitleAlertDialog

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    /**
     * 显示警告弹窗
     *
     * @param context    context
     * @param title      标题
     * @param content    内容
     * @param confirm    确认键
     * @param cancelable 点击空白处是否消失
     */
    protected fun showAlertDialog(
        context: Context?,
        title: String?,
        content: String?,
        confirm: String?,
        cancelable: Boolean
    ) {
        val mAlertDialog = AlertDialog(context, title, content, confirm)
        mAlertDialog.setOnDialogClickListener { mAlertDialog.dismiss() }
        // 点击空白处消失
        mAlertDialog.setCancelable(cancelable)
        mAlertDialog.show()
    }

    /**
     * 显示警告弹窗(无标题)
     *
     * @param context context
     * @param content 内容
     * @param confirm 确认键
     */
    protected fun showNoTitleAlertDialog(context: Context?, content: String?, confirm: String?) {
        val mNoTitleAlertDialog = NoTitleAlertDialog(context, content, confirm)
        mNoTitleAlertDialog.setOnDialogClickListener { mNoTitleAlertDialog.dismiss() }
        // 点击空白处消失
        mNoTitleAlertDialog.setCancelable(true)
        mNoTitleAlertDialog.show()
    }

}