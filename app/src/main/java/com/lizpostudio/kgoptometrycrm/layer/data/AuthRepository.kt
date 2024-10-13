package com.lizpostudio.kgoptometrycrm.layer.data

import android.content.Context
import com.lizpostudio.kgoptometrycrm.layer.data.local.AppDatabase
import com.lizpostudio.kgoptometrycrm.layer.data.local.dao.DeviceDao
import com.lizpostudio.kgoptometrycrm.layer.data.local.dao.UserDao
import com.lizpostudio.kgoptometrycrm.layer.data.local.entity.DeviceEntity
import com.lizpostudio.kgoptometrycrm.layer.data.local.entity.UserEntity
import com.lizpostudio.kgoptometrycrm.layer.data.remote.AuthRemoteDataSource
import com.lizpostudio.kgoptometrycrm.layer.domain.model.ConfigModel
import com.lizpostudio.kgoptometrycrm.layer.domain.model.Resources
import com.lizpostudio.kgoptometrycrm.layer.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn

class AuthRepository private constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userDao: UserDao,
    private val deviceDao: DeviceDao
) {

    companion object {

        @Volatile
        private var _sInstance: AuthRepository? = null

        fun getInstance(context: Context) = _sInstance ?: synchronized(this) {
            _sInstance ?: run {
                val db = AppDatabase.getInstance(context)
                AuthRepository(
                    authRemoteDataSource = AuthRemoteDataSource.getInstance(context),
                    userDao = db.userDao,
                    deviceDao = db.deviceDao
                ).also { repo -> _sInstance = repo }
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
        val isSet = authRemoteDataSource.setDefaultFirebaseConfig(context)
        if (isSet)
            userDao.deleteAll()
        return isSet
    }

    fun signIn(email: String, password: String) = callbackFlow {
        trySend(Resources.Loading())
        val authResult = authRemoteDataSource.signIn(email, password)
        val firebaseUser = authResult?.user
        if (firebaseUser != null) {
            val uid = firebaseUser.uid
            val email = firebaseUser.email
            val projectID = authRemoteDataSource.getProjectId()
            val currentDeviceEntity = deviceDao.selectByProjectIdAsFlow(projectID).firstOrNull()
            val deviceEntity = authRemoteDataSource.generateDeviceCode<DeviceEntity>(
                getCurrentCode = { currentDeviceEntity?.code },
                doTransaction = { code, isTrusted ->
                    if (code.isNullOrBlank())
                        return@generateDeviceCode null
                    if (currentDeviceEntity == null) {
                        val newDeviceEntity =
                            DeviceEntity(
                                projectID = projectID,
                                code = code,
                                isTrusted = isTrusted
                            )
                        if (deviceDao.insert(newDeviceEntity) > 0)
                            return@generateDeviceCode newDeviceEntity
                        return@generateDeviceCode null
                    }
                    return@generateDeviceCode currentDeviceEntity
                },
            )
            if (deviceEntity == null) {
                authRemoteDataSource.signOut()
                close(IllegalStateException("Can't create device code, please try again"))
                return@callbackFlow
            }
            val userEntity = userDao.selectByID(uid)
            if (userEntity != null) {
                val userModel = User(
                    uid = userEntity.uid,
                    email = userEntity.email,
                    isTrusted = deviceEntity.isTrusted,
                    isAdmin = userEntity.isAdmin
                )
                userDao.updateIsSelectAndInsert(userEntity.copy(isSelect = true))
                trySend(Resources.Success(userModel))
            } else {
                val newUserEntity = UserEntity(
                    uid = uid,
                    email = email,
                    isAdmin = isAdmin(email),
                    isSelect = true
                )
                val resInsertUserEntity = userDao.updateIsSelectAndInsert(newUserEntity)
                if (resInsertUserEntity > 0) {
                    val userModel = User(
                        uid = newUserEntity.uid,
                        email = newUserEntity.email,
                        isTrusted = deviceEntity.isTrusted,
                        isAdmin = newUserEntity.isAdmin
                    )
                    trySend(Resources.Success(userModel))
                } else {
                    authRemoteDataSource.signOut()
                    close(Throwable("Database error"))
                }
            }
        } else {
            close(Throwable("Authentication failed."))
        }
        awaitClose()
    }.catch { emit(Resources.Failure(it)) }
        .flowOn(Dispatchers.IO)

    private fun isAdmin(email: String?) =
        if (!email.isNullOrBlank()) {
            email.split("@")[0].lowercase() == "admin"
        } else
            false

}
