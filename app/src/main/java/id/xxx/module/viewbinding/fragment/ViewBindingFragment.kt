package id.xxx.module.viewbinding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.delegate.ViewBinding.Companion.getMethodInflate
import androidx.viewbinding.ViewBinding
import id.xxx.module.viewbinding.ktx.viewBinding
import java.lang.reflect.ParameterizedType

abstract class ViewBindingFragment<T : ViewBinding> : Fragment() {

    protected val viewBinding by viewBinding<T>()

    private fun <T : ViewBinding> viewBinding(
        viewGroup: () -> ViewGroup? = { null },
        attachToParent: Boolean = false,
        onDestroy: (T) -> Unit = {},
    ): androidx.fragment.app.delegate.ViewBinding<T> {
        val types =
            (javaClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments
        types?.forEach { type ->
            @Suppress("UNCHECKED_CAST")
            val viewBindingClass = (type as? Class<T>)
            if (viewBindingClass?.getMethodInflate() != null) {
                return viewBinding(
                    viewBindingClass = viewBindingClass,
                    viewGroup = viewGroup,
                    attachToParent = attachToParent,
                    onDestroy = onDestroy,
                )
            }
        }
        throw Error()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = viewBinding.root
        if (view.background == null)
            view.background = activity?.window?.decorView?.background
        return view
    }
}