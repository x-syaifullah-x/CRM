package id.xxx.module.viewbinding.fragment

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.delegate.ViewBinding.Companion.inflate
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class ViewBindingFragmentActivity<T : ViewBinding> : FragmentActivity() {

    protected val viewBinding by viewBinding<T>()

    fun <T : ViewBinding> viewBinding(
        parent: ViewGroup? = null,
        attachToParent: Boolean = false
    ): Lazy<T> {
        val types =
            (javaClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments
        @Suppress("UNCHECKED_CAST")
        types?.forEach { type ->
            val viewBindingClass = (type as? Class<T>)
            val result = viewBindingClass?.inflate(layoutInflater, parent, attachToParent)
            if (result != null) {
                return lazy(LazyThreadSafetyMode.NONE) { result }
            }
        }

        throw Error()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(viewBinding.root)
    }
}