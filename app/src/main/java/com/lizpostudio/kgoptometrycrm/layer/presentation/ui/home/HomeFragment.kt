package com.lizpostudio.kgoptometrycrm.layer.presentation.ui.home

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.lizpostudio.kgoptometrycrm.R
import com.lizpostudio.kgoptometrycrm.databinding.FragmentHomeBinding
import com.lizpostudio.kgoptometrycrm.layer.domain.model.Resources
import com.lizpostudio.kgoptometrycrm.layer.domain.model.User
import com.lizpostudio.kgoptometrycrm.layer.domain.throwable.LoginFirstThrowable
import com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in.SignInFragment
import id.xxx.module.viewbinding.ktx.viewBinding

class HomeFragment : Fragment() {

    companion object {

        const val KEY_USER_ID = "key_user_id"
    }

    private val vBinding by viewBinding<FragmentHomeBinding>()
    private val viewModel by viewModels<HomeViewModel> { HomeViewModel.Factory }
    private val observerUser = object : Observer<Resources<User>> {
        override fun onChanged(res: Resources<User>) {
            when (res) {
                is Resources.Loading -> {
                    vBinding.helloUserText.text = getString(R.string.please_wait)
                }

                is Resources.Success -> {
                    val user = res.value
                    val email = (user.email ?: "-").uppercase()
                    val helloText = "Hello, ${email.split("@")[0]}!"
                    vBinding.helloUserText.text = helloText
                    vBinding.settingsButton.isVisible = user.isAdmin
                    vBinding.startButton.setOnClickListener { v ->
                        if (!user.isTrusted) {
                            showPopup(
                                requireContext(),
                                "Your device is not recognized!\nPlease, contact your administrator to proceed!"
                            )
                        } else {
                            showPopup(
                                requireContext(),
                                "Trusted && ${user.isAdmin}"
                            )
                        }
                    }
                }

                is Resources.Failure -> {
                    val err = res.value
                    if (err is LoginFirstThrowable) {
                        moveToSignFragment()
                        return
                    }
                    AlertDialog.Builder(vBinding.root.context)
                        .setTitle("Error")
                        .setMessage(res.value.message)
                        .setPositiveButton("Try Again") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }

    }

    val userID by lazy { arguments?.getString(KEY_USER_ID) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = vBinding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setUser(userID)
        viewModel.user.observe(viewLifecycleOwner, observerUser)

        vBinding.btnLogOut.setOnClickListener { v -> signOut(v.context) }
    }

    private fun signOut(context: Context?) {
        viewModel.signOut(userID).observe(viewLifecycleOwner) { res: Resources<Unit> ->
            when (res) {
                is Resources.Loading -> {
                    vBinding.btnLogOut.visibility = View.INVISIBLE
                    vBinding.pbLogOut.visibility = View.VISIBLE
                }

                is Resources.Success -> viewModel.setUser(null)

                is Resources.Failure -> {
                    vBinding.btnLogOut.visibility = View.VISIBLE
                    vBinding.pbLogOut.visibility = View.INVISIBLE
                    Toast.makeText(context, res.value.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun moveToSignFragment() = activity?.supportFragmentManager
        ?.beginTransaction()
        ?.replace(android.R.id.content, SignInFragment::class.java, null)
        ?.commitNow()

    private fun showPopup(context: Context, message: String) {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val popupView: View =
            layoutInflater.inflate(R.layout.popup_action_info, vBinding.root, false)
        val textItem = popupView.findViewById<TextView>(R.id.popup_text)
        textItem.text = message
        val width: Int = LinearLayout.LayoutParams.WRAP_CONTENT
        val height: Int = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        popupView.setOnTouchListener { v, _ ->
            if (v.performClick()) {
                popupWindow.dismiss()
            }
            true
        }
    }
}