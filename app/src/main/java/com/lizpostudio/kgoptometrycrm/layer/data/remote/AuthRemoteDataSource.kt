package com.lizpostudio.kgoptometrycrm.layer.data.remote

import android.content.Context
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Query
import com.google.firebase.database.Transaction
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

        fun getInstance(context: Context) = _sInstance ?: synchronized(this) {
            _sInstance ?: AuthRemoteDataSource().also {
                it.setFirebaseConfig(context, null)
                _sInstance = it
            }
        }
    }

    fun setDefaultFirebaseConfig(context: Context) =
        MyFirebase.setDefaultFirebaseConfig(context)

    fun setFirebaseConfig(context: Context, config: ConfigModel?) =
        MyFirebase.setConfig(context, config)

    fun signOut() =
        MyFirebase.getFirebaseAuth().signOut()

    fun getProjectId() = MyFirebase.getFirebaseApp().options.projectId
        ?: throw NullPointerException("Please set config before call this method")

    suspend fun <T> generateDeviceCode(
        getCurrentCode: suspend () -> String?,
        doTransaction: suspend (code: String?, isTrusted: Boolean) -> T?,
    ): T? {
        val firebaseDatabase = MyFirebase.getFirebaseDatabase()
        val settings = firebaseDatabase.reference.child("settings")
        val devices = settings.child("devices")
        val trusted = devices.child("trusted")
        val new = devices.child("new")

        val currentCode = getCurrentCode.invoke()

        if (currentCode != null) {
            val exists = trusted
                .orderByValue()
                .equalTo(currentCode)
                .get()
                .await()
                .exists()
            if (exists)
                return doTransaction(currentCode, true)
        }

        val code = currentCode ?: (1..10)
            .map { i -> Random.nextInt(i, CHAR_POOL.size) }
            .map(CHAR_POOL::get)
            .joinToString("")
        val isInNewDevices = new.orderByValue().equalTo(code).get().await().exists()
        if (isInNewDevices)
            return doTransaction(code, false)

        val format = SimpleDateFormat("dd_MM_yy_hh_mm_ss", Locale.getDefault())
            .format(System.currentTimeMillis())
        val newDeviceCodeKey = "${format}_M_${android.os.Build.MODEL}"
        val newDeviceCodeRef = new.child(newDeviceCodeKey)
        val exists = trusted
            .orderByValue()
            .equalTo(code)
            .get()
            .await()
            .exists()
        if (exists)
            return null
        val res = doTransaction.invoke(code, false)
        newDeviceCodeRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val isSuccess = res != null
                if (isSuccess) {
                    currentData.value = code
                    return Transaction.success(currentData)
                }
                return Transaction.abort()
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
            }
        })
        return res
    }

    suspend fun signIn(email: String, password: String): AuthResult? {
        val firebaseAuth = MyFirebase.getFirebaseAuth()
        val firebaseDatabase = MyFirebase.getFirebaseDatabase()
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

    fun queryTrustedDevices(code: String?): Query {
        return MyFirebase.getFirebaseDatabase()
            .reference
            .child("settings")
            .child("devices")
            .child("trusted")
            .orderByValue()
            .equalTo(code)
    }
}