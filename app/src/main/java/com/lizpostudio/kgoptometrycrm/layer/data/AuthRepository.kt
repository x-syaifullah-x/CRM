package com.lizpostudio.kgoptometrycrm.layer.data

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lizpostudio.kgoptometrycrm.layer.data.local.AppDatabase
import com.lizpostudio.kgoptometrycrm.layer.data.local.dao.UserDao
import com.lizpostudio.kgoptometrycrm.layer.data.local.entity.UserEntity
import com.lizpostudio.kgoptometrycrm.layer.data.remote.AuthRemoteDataSource
import com.lizpostudio.kgoptometrycrm.layer.domain.model.ConfigModel
import com.lizpostudio.kgoptometrycrm.layer.domain.model.Resources
import com.lizpostudio.kgoptometrycrm.layer.domain.model.User
import com.lizpostudio.kgoptometrycrm.layer.domain.throwable.LoginFirstThrowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class AuthRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userDao: UserDao
) {

    companion object {

        @Volatile
        private var _sInstance: AuthRepository? = null

        fun getInstance(context: Context) = _sInstance ?: synchronized(this) {
            _sInstance ?: AuthRepository(
                authRemoteDataSource = AuthRemoteDataSource.getInstance(),
                userDao = AppDatabase.getInstance(context).userDao
            ).also { repo ->
                repo.authRemoteDataSource.setFirebaseConfig(context, null)
                _sInstance = repo
            }
        }
    }

    fun changeFirebaseConfig(context: Context, config: ConfigModel): Boolean {
        val isConfigChange = authRemoteDataSource.setFirebaseConfig(context, config)
        if (isConfigChange) {
            userDao.deleteAll()
        }
        return isConfigChange
    }

    fun setDefaultFirebaseConfig(context: Context): Boolean {
        val pref = AuthRemoteDataSource.getPreferencesFirebaseConfig(context)
        val edit = pref.edit()
        edit.remove("project_number")
        edit.remove("firebase_url")
        edit.remove("storage_bucket")
        edit.remove("project_id")
        edit.remove("api_key")
        edit.remove("mobile_sdk_app_id")
        if (edit.commit()) {
            userDao.deleteAll()
            return authRemoteDataSource.setFirebaseConfig(context, null)
        }
        return false
    }

    fun signIn(email: String, password: String) = flow {
        emit(Resources.Loading())
        val authResult = authRemoteDataSource.signIn(email, password)
        val firebaseUser = authResult?.user
        if (firebaseUser != null) {
            val entity = userDao.selectUserByID(firebaseUser.uid)
            val newEntity =
                if (entity != null) {
                    entity.copy(email = firebaseUser.email)
                } else {
                    val isAdmin = if (firebaseUser.email != null) {
                        val email = firebaseUser.email ?: ""
                        email.split("@")[0].lowercase() == "admin"
                    } else {
                        false
                    }
                    if (isAdmin) {
                        UserEntity(
                            id = firebaseUser.uid,
                            email = firebaseUser.email,
                            deviceCode = "",
                            isTrusted = true,
                            isAdmin = true
                        )
                    } else {
                        val deviceCode = authRemoteDataSource.generateDeviceCode()
                        if (deviceCode == null) {
                            val error = Throwable("Failed to get device code, please try again.")
                            emit(Resources.Failure(error))
                            return@flow
                        }
                        UserEntity(
                            id = firebaseUser.uid,
                            email = firebaseUser.email,
                            deviceCode = deviceCode,
                            isTrusted = false,
                            isAdmin = false

                        )
                    }
                }
            val resInsert = userDao.insert(newEntity)
            if (resInsert > 0) {
                val user = User(
                    uid = newEntity.id,
                    email = newEntity.email,
                    isTrusted = newEntity.isTrusted,
                    isAdmin = newEntity.isAdmin
                )
                emit(Resources.Success(user))
            } else {
                authRemoteDataSource.signOut()
                emit(Resources.Failure(Throwable("Database error")))
            }
        } else {
            val error = Throwable("Authentication failed.")
            emit(Resources.Failure(error))
        }
    }.catch { emit(Resources.Failure(it)) }
        .flowOn(Dispatchers.IO)

    fun getUser(userID: String?) = callbackFlow<Resources<User>> {
        trySend(Resources.Loading())
        val job = launch {
            val trustedListener = object : ValueEventListener {
                var userEntity: UserEntity? = null
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userEntity = this.userEntity
                    if (userEntity != null) {
                        if (userEntity.isAdmin)
                            return
                        val isTrusted = snapshot.value != null
                        val newUserEntity = userEntity.copy(isTrusted = isTrusted)
                        if (userEntity != newUserEntity)
                            userDao.insert(newUserEntity)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            userDao.selectUserByIDAsFlow(userID).collect { e: UserEntity? ->
                val trusted = authRemoteDataSource.getFirebaseDatabase()?.reference
                    ?.child("settings")
                    ?.child("devices")
                    ?.child("trusted")
                    ?.orderByValue()
                    ?.equalTo(e?.deviceCode)
                if (e == null) {
                    trustedListener.userEntity = null
                    trusted?.removeEventListener(trustedListener)
                    close(LoginFirstThrowable())
                } else {
                    trustedListener.userEntity = e
                    trusted?.removeEventListener(trustedListener)
                    trusted?.addValueEventListener(trustedListener)
                    val user =
                        User(
                            uid = e.id,
                            email = e.email,
                            isTrusted = e.isTrusted,
                            isAdmin = e.isAdmin
                        )
                    trySend(Resources.Success(user))
                    val auth = authRemoteDataSource.getFirebaseAuth()
                    auth?.currentUser?.reload()
                    if (auth?.currentUser == null) {
                        close(LoginFirstThrowable())
                    }
                    if (auth?.currentUser?.uid != userID) {
                        close(LoginFirstThrowable())
                    }
                }
            }
        }
        awaitClose { job.cancel() }
    }.catch { emit(Resources.Failure(it)) }
        .flowOn(Dispatchers.IO)

    fun signOut(userID: String?) = flow<Resources<Unit>> {
        if (userID.isNullOrBlank()) {
            emit(Resources.Failure(LoginFirstThrowable()))
            return@flow
        }
        authRemoteDataSource.signOut()
        emit(Resources.Success(Unit))
    }.catch { emit(Resources.Failure(it)) }
        .flowOn(Dispatchers.IO)

}
