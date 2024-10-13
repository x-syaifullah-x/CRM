package androidx.fragment.app.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class ViewBinding<ViewBindingClass : ViewBinding>(
    private val viewBindingClass: KClass<ViewBindingClass>,
    private val viewGroup: () -> ViewGroup? = { null },
    private val attachToParent: Boolean = false,
    private val onDestroy: (ViewBindingClass) -> Unit = { },
) : ReadOnlyProperty<Fragment, ViewBindingClass> {

    companion object {

        @JvmStatic
        fun <T : ViewBinding> Class<T>.getMethodInflate(): Method? =
            try {
                getMethod(
                    "inflate",
                    LayoutInflater::class.java,
                    ViewGroup::class.java,
                    Boolean::class.java
                )
            } catch (t: Throwable) {
                null
            }

        @JvmStatic
        fun <T : ViewBinding> KClass<T>.inflate(
            layoutInflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean
        ) = java.inflate(layoutInflater, parent, attachToParent)

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : ViewBinding> Class<T>.inflate(
            layoutInflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean
        ) = getMethodInflate()?.invoke(null, layoutInflater, parent, attachToParent) as? T
    }

    private var _bindingClass: ViewBindingClass? = null

    private val lifecycleEventObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                onDestroy(_bindingClass ?: return)
                _bindingClass = null
            }
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>) =
        _bindingClass ?: synchronized(this) {
            _bindingClass ?: run {
                thisRef.viewLifecycleOwnerLiveData.observe(thisRef) { lifecycleOwner ->
                    lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)
                }
                _bindingClass = viewBindingClass
                    .inflate(thisRef.layoutInflater, viewGroup.invoke(), attachToParent)
                return _bindingClass ?: throw NullPointerException()
            }
        }
}