package com.lizpostudio.kgoptometrycrm.layer.presentation.constant

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {

    const val KEY_UID = "uid"

    @Volatile
    private var _sPreference: SharedPreferences? = null

    private fun getSharedPreferences(context: Context?) = _sPreference
        ?: synchronized(this) {
            _sPreference ?: context?.getSharedPreferences(
                "${context.packageName}_preferences",
                Context.MODE_PRIVATE
            ).also { _sPreference = it }
        }

    fun getUserID(context: Context?) =
        getSharedPreferences(context)?.getString(KEY_UID, null)

    fun setUserID(context: Context?, uid: String?): Boolean {
        val sp = getSharedPreferences(context)
        val edit = sp?.edit() ?: return false
        if (uid.isNullOrBlank()) {
            edit.remove(KEY_UID)
        } else {
            edit.putString(KEY_UID, uid)
        }
        return edit.commit()
    }
}