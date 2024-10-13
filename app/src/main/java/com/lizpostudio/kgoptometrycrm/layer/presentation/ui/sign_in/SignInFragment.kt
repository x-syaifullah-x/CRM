package com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lizpostudio.kgoptometrycrm.databinding.FragmentSignInBinding
import com.lizpostudio.kgoptometrycrm.ktx.hideKeyboard
import com.lizpostudio.kgoptometrycrm.layer.domain.model.Resources
import com.lizpostudio.kgoptometrycrm.layer.presentation.ui.home.HomeFragment
import com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in.settings.SignInSettingFragment
import id.xxx.module.viewbinding.ktx.viewBinding

class SignInFragment : Fragment() {

    private val vBinding by viewBinding<FragmentSignInBinding>()
    private val viewModel by viewModels<SignInViewModel> { SignInViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = vBinding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val fragments = childFragmentManager.fragments
                val fragment = fragments.removeLastOrNull()
                if (fragment != null)
                    childFragmentManager.beginTransaction().remove(fragment).commit()
                isEnabled = fragments.isNotEmpty()
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, onBackPressedCallback)

        vBinding.tvSetting.setOnClickListener { _ ->
            vBinding.flSetting.bringToFront()
            childFragmentManager.beginTransaction().replace(
                vBinding.flSetting.id,
                SignInSettingFragment::class.java,
                null
            ).commit()
            onBackPressedCallback.isEnabled = true
        }
        vBinding.btnLogin.setOnClickListener { v ->
            hideKeyboard(v) {
                val email = "${vBinding.editLoginName.text}@gmail.com"
                val password = "${vBinding.editPassword.text}"
                signIn(email, password)
            }
        }
    }

    private fun signIn(email: String, password: String) {
        if (email.isNotBlank() && password.isNotBlank()) {
            viewModel.signIn(email, password).observe(viewLifecycleOwner) { res ->
                println(res)
                when (res) {
                    is Resources.Loading -> {
                        vBinding.btnLogin.visibility = View.INVISIBLE
                        vBinding.pbLogin.visibility = View.VISIBLE
                    }

                    is Resources.Success -> {
                        vBinding.btnLogin.visibility = View.VISIBLE
                        vBinding.pbLogin.visibility = View.INVISIBLE
                        val userID = res.value.uid
                        activity?.supportFragmentManager
                            ?.beginTransaction()
                            ?.replace(
                                android.R.id.content,
                                HomeFragment::class.java,
                                bundleOf(HomeFragment.KEY_USER_ID to userID)
                            )
                            ?.commit()
                    }

                    is Resources.Failure -> {
                        vBinding.btnLogin.visibility = View.VISIBLE
                        vBinding.pbLogin.visibility = View.INVISIBLE
                        Toast.makeText(context, res.value.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}