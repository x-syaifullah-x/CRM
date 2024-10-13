package com.lizpostudio.kgoptometrycrm.layer.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.lizpostudio.kgoptometrycrm.R
import com.lizpostudio.kgoptometrycrm.layer.data.UserRepository
import com.lizpostudio.kgoptometrycrm.layer.presentation.ui.home.HomeFragment
import com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in.SignInFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = window.decorView

        // BEGIN SET DARK STATUS BAR
        val windowInsetController = WindowInsetsControllerCompat(window, view)
        windowInsetController.isAppearanceLightStatusBars = true
        // END SET DARK STATUS BAR

        lifecycleScope.launch {
            val user = UserRepository.getInstance(this@MainActivity).getCurrentUser()
            val uid = user?.uid
            val pair =
                if (uid.isNullOrBlank())
                    SignInFragment::class.java to null
                else
                    HomeFragment::class.java to bundleOf(HomeFragment.KEY_USER_ID to uid)
            view.setBackgroundResource(R.color.lightBackground)
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, pair.first, pair.second)
                .commitNow()
        }
    }
}
