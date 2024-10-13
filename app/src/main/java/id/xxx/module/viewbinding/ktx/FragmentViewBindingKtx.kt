@file:JvmName("FragmentViewBindingKtx")

package id.xxx.module.viewbinding.ktx

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass

inline fun <reified T : ViewBinding> Fragment.viewBinding(
    noinline viewGroup: () -> ViewGroup? = { null },
    attachToParent: Boolean = false,
    noinline onDestroy: (T) -> Unit = {},
) = viewBinding(
    viewBindingClass = T::class,
    viewGroup = viewGroup,
    attachToParent = attachToParent,
    onDestroy = onDestroy,
)

fun <T : ViewBinding> Fragment.viewBinding(
    viewBindingClass: Class<T>,
    viewGroup: () -> ViewGroup? = { null },
    attachToParent: Boolean = false,
    onDestroy: (T) -> Unit = { },
) = viewBinding(
    viewBindingClass = viewBindingClass.kotlin,
    viewGroup = viewGroup,
    attachToParent = attachToParent,
    onDestroy = onDestroy
)

fun <T : ViewBinding> Fragment.viewBinding(
    viewBindingClass: KClass<T>,
    viewGroup: () -> ViewGroup? = { null },
    attachToParent: Boolean = false,
    onDestroy: (T) -> Unit = { },
) = androidx.fragment.app.delegate.ViewBinding(
    viewBindingClass = viewBindingClass,
    viewGroup = viewGroup,
    attachToParent = attachToParent,
    onDestroy = onDestroy
)