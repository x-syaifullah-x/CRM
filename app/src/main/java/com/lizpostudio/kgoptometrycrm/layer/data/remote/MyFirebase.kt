package com.lizpostudio.kgoptometrycrm.layer.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lizpostudio.kgoptometrycrm.layer.domain.model.ConfigModel

object MyFirebase {

    @Volatile
    private var _firebaseApp: FirebaseApp? = null

    fun getFirebaseApp() = _firebaseApp
        ?: throw NullPointerException("Firebase App Not configure")

    fun getFirebaseAuth() =
        FirebaseAuth.getInstance(getFirebaseApp())

    fun getFirebaseDatabase() =
        FirebaseDatabase.getInstance(getFirebaseApp())

    fun setDefaultFirebaseConfig(context: Context): Boolean {
        val pref = getPreferencesFirebaseConfig(context)
        val edit = pref.edit()
        edit.remove("project_number")
        edit.remove("firebase_url")
        edit.remove("storage_bucket")
        edit.remove("project_id")
        edit.remove("api_key")
        edit.remove("mobile_sdk_app_id")
        if (edit.commit()) {
            return setConfig(context, null)
        }
        return false
    }

    fun setConfig(context: Context, config: ConfigModel?): Boolean {
        return synchronized(this) {
            if (_firebaseApp != null)
                _firebaseApp?.delete()
            val option = FirebaseOptions.Builder()
            if (config == null) {
                val sp = getPreferencesFirebaseConfig(context)
                val projectNumber = sp.getString("project_number", "630259719920")
                val firebaseUrl =
                    sp.getString("firebase_url", "https://kgoptometrycrm.firebaseio.com")
                val storageBucket = sp.getString("storage_bucket", "kgoptometrycrm.appspot.com")
                val projectId = sp.getString("project_id", "kgoptometrycrm")
                val apiKey = sp.getString("api_key", "AIzaSyBI6-DpeH-ki0jLsQ64E3XVrw00wxG-qQI")
                val mobileSdkAppId =
                    sp.getString(
                        "mobile_sdk_app_id",
                        "1:630259719920:android:02d8acd58e5fc3ad0d2c35"
                    )
                option.setDatabaseUrl(firebaseUrl)
                option.setGcmSenderId(projectNumber)
                option.setApiKey("$apiKey")
                option.setApplicationId("$mobileSdkAppId")
                option.setStorageBucket(storageBucket)
                option.setProjectId(projectId)
                _firebaseApp = FirebaseApp.initializeApp(context, option.build())
                return true
            }
            option.setDatabaseUrl(config.firebaseUrl)
            option.setGcmSenderId(config.projectNumber)
            option.setApiKey(config.apiKey)
            option.setApplicationId(config.mobileSdkAppId)
            option.setStorageBucket(config.storageBucket)
            option.setProjectId(config.projectId)
            val sharedPreferences = getPreferencesFirebaseConfig(context)
            val edit = sharedPreferences.edit()
            edit.putString("project_number", config.projectNumber)
            edit.putString("firebase_url", config.firebaseUrl)
            edit.putString("storage_bucket", config.storageBucket)
            edit.putString("project_id", config.projectId)
            edit.putString("api_key", config.apiKey)
            edit.putString("mobile_sdk_app_id", config.mobileSdkAppId)
            if (edit.commit()) {
                _firebaseApp = FirebaseApp.initializeApp(context, option.build())
                return true
            }
            return false
        }
    }

    private fun getPreferencesFirebaseConfig(context: Context) =
        context.getSharedPreferences("firebase_config", Context.MODE_PRIVATE)
}