@file:JvmName("ActivityViewBindingKtx")

package id.xxx.module.viewbinding.ktx

import android.app.Activity
import android.view.ViewGroup
import androidx.fragment.app.delegate.ViewBinding.Companion.inflate
import androidx.viewbinding.ViewBinding

inline fun <reified T : ViewBinding> Activity.viewBinding(
    parent: ViewGroup? = null,
    attachToParent: Boolean = false
) = lazy(LazyThreadSafetyMode.NONE) {
    T::class.java.inflate(layoutInflater, parent, attachToParent)
        ?: throw NullPointerException()
}