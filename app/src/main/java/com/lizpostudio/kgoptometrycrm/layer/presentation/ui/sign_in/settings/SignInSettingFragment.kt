package com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lizpostudio.kgoptometrycrm.databinding.FragmentSignInSettingsBinding
import id.xxx.module.viewbinding.ktx.viewBinding

class SignInSettingFragment : Fragment() {

    private val binding by viewBinding<FragmentSignInSettingsBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        childFragmentManager
            .beginTransaction()
            .replace(binding.container.id, SignInSettingPreferenceFragment())
            .commit()

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return binding.root
    }
}