package com.lizpostudio.kgoptometrycrm.layer.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lizpostudio.kgoptometrycrm.layer.domain.model.ConfigModel
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

class AuthRemoteDataSource() {

    companion object {

        private val CHAR_POOL: List<Char> = ('A'..'Z') + ('0'..'9')

        @Volatile
        private var _sInstance: AuthRemoteDataSource? = null

        fun getInstance() = _sInstance ?: synchronized(this) {
            _sInstance ?: AuthRemoteDataSource().also { _sInstance = it }
        }

        fun getPreferencesFirebaseConfig(context: Context) =
            context.getSharedPreferences("firebase_config", Context.MODE_PRIVATE)
    }

    @Volatile
    private var _firebaseApp: FirebaseApp? = null

    fun setFirebaseConfig(context: Context, config: ConfigModel?): Boolean {
        if (_firebaseApp != null)
            _firebaseApp?.delete()
        val option = FirebaseOptions.Builder()
        if (config == null) {
            val sp = getPreferencesFirebaseConfig(context)
            val projectNumber = sp.getString("project_number", "630259719920")
            val firebaseUrl = sp.getString("firebase_url", "https://kgoptometrycrm.firebaseio.com")
            val storageBucket = sp.getString("storage_bucket", "kgoptometrycrm.appspot.com")
            val projectId = sp.getString("project_id", "kgoptometrycrm")
            val apiKey = sp.getString("api_key", "AIzaSyBI6-DpeH-ki0jLsQ64E3XVrw00wxG-qQI")
            val mobileSdkAppId =
                sp.getString("mobile_sdk_app_id", "1:630259719920:android:02d8acd58e5fc3ad0d2c35")
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

    fun signOut() {
        val firebaseApp = _firebaseApp
        if (firebaseApp == null)
            throw NullPointerException()
        val firebaseAuth = FirebaseAuth.getInstance(firebaseApp)
        return firebaseAuth.signOut()
    }

    suspend fun generateDeviceCode(): String? {
        val firebaseApp = _firebaseApp
        if (firebaseApp == null)
            return null
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val settings = firebaseDatabase.reference.child("settings")
        val devices = settings.child("devices")
        val trusted = devices.child("trusted")
        val new = devices.child("new")
        val id = (1..10)
            .map { i -> Random.nextInt(i, CHAR_POOL.size) }
            .map(CHAR_POOL::get)
            .joinToString("")
        val exists = trusted
            .orderByValue()
            .equalTo(id)
            .get()
            .await()
            .exists()
        if (exists) {
            return null
        }
        val newDeviceCodeKey = "${
            SimpleDateFormat(
                "dd_MM_yy_hh_mm_ss",
                Locale.getDefault(),
            ).format(System.currentTimeMillis())
        }_M_${android.os.Build.MODEL}"
        new.child(newDeviceCodeKey).setValue(id).await()
        return id
    }

    suspend fun signIn(email: String, password: String): AuthResult? {
        val firebaseApp = _firebaseApp
        if (firebaseApp == null)
            throw NullPointerException()

        val firebaseAuth = FirebaseAuth.getInstance(firebaseApp)
        val firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp)
        val authResult = firebaseAuth
            .signInWithEmailAndPassword(email, password).await()
        val user = authResult.user
        if (user != null) {
            val uid = user.uid
            val email = user.email
            firebaseDatabase.reference
                .child("users")
                .child("login")
                .child(uid)
                .setValue(
                    mapOf(
                        "uid" to uid,
                        "timestamp" to System.currentTimeMillis(),
                        "email" to email
                    )
                )
        }
        return authResult
    }

    fun getFirebaseDatabase(): FirebaseDatabase? {
        return FirebaseDatabase.getInstance(_firebaseApp ?: return null)
    }

    fun getFirebaseAuth(): FirebaseAuth? {
        return FirebaseAuth.getInstance(_firebaseApp ?: return null)
    }
}