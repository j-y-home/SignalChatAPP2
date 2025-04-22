package com.lnsoft.conslutationsystem.core

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Activity 管理器
 */
object AppActivityManager {

    private var sCurrentActivityWeakRef: WeakReference<Activity>? = null

    fun getCurrentActivity(): Activity? {
        var currentActivity: Activity? = null
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef?.get()
        }
        return currentActivity
    }

    fun setCurrentActivity(activity: Activity) {
        sCurrentActivityWeakRef = WeakReference(activity)
    }

}