package com.lizpostudio.kgoptometrycrm.layer.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.lizpostudio.kgoptometrycrm.R
import com.lizpostudio.kgoptometrycrm.layer.presentation.constant.AppPreferences
import com.lizpostudio.kgoptometrycrm.layer.presentation.ui.home.HomeFragment
import com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in.SignInFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val fragmentLifecycleCB = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, bundle: Bundle?) {
            when (fragment) {
                is SignInFragment -> AppPreferences.setUserID(fragment.context, null)
                is HomeFragment -> AppPreferences.setUserID(fragment.context, fragment.userID)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = window.decorView

        // BEGIN SET DARK STATUS BAR
        val windowInsetController = WindowInsetsControllerCompat(window, view)
        windowInsetController.isAppearanceLightStatusBars = true
        // END SET DARK STATUS BAR

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCB, true)

        lifecycleScope.launch {
            delay(600)
            view.setBackgroundResource(R.color.lightBackground)
            val uid = AppPreferences.getUserID(this@MainActivity)
            val pair =
                if (uid.isNullOrBlank())
                    SignInFragment::class.java to null
                else
                    HomeFragment::class.java to bundleOf(HomeFragment.KEY_USER_ID to uid)
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, pair.first, pair.second)
                .commitNow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCB)
    }
}
