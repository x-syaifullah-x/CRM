@file:JvmName("EspressoIdlingResource")

package com.lizpostudio.kgoptometrycrm.test.espresso

import androidx.test.espresso.idling.CountingIdlingResource
import com.lizpostudio.kgoptometrycrm.layer.presentation.constant.AppBuild

object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    val idlingResource by lazy { CountingIdlingResource(RESOURCE) }

    @JvmStatic
    fun increment() {
        if (AppBuild.IS_DEBUG)
            idlingResource.increment()
    }

    @JvmStatic
    fun decrement() {
        if (AppBuild.IS_DEBUG)
            idlingResource.decrement()
    }
}