package com.lizpostudio.kgoptometrycrm

import android.app.Activity
import android.app.Application

open class App : Application() {

    private var isFirstLoadState: Boolean? = true

    fun isFirstTimeLoad(): Boolean {
        return isFirstLoadState == true
    }

    fun <T : Activity> firstTimeLoadFinish(activity: T) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val isActivityMain = intent?.component?.className == activity::class.java.name
        if (isActivityMain && isFirstLoadState == true)
            isFirstLoadState = null
    }
}