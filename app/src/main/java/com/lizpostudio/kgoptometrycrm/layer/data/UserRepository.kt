package com.lizpostudio.kgoptometrycrm.layer.data

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lizpostudio.kgoptometrycrm.layer.data.local.AppDatabase
import com.lizpostudio.kgoptometrycrm.layer.data.local.dao.DeviceDao
import com.lizpostudio.kgoptometrycrm.layer.data.local.dao.UserDao
import com.lizpostudio.kgoptometrycrm.layer.data.remote.AuthRemoteDataSource
import com.lizpostudio.kgoptometrycrm.layer.domain.model.Resources
import com.lizpostudio.kgoptometrycrm.layer.domain.model.User
import com.lizpostudio.kgoptometrycrm.layer.domain.throwable.LoginFirstThrowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UserRepository(
    private val remoteDataSource: AuthRemoteDataSource,
    private val userDao: UserDao,
    private val deviceDao: DeviceDao
) {

    companion object {

        @Volatile
        private var _sInstance: UserRepository? = null

        fun getInstance(context: Context) = _sInstance ?: synchronized(this) {
            _sInstance ?: run {
                val db = AppDatabase.getInstance(context)
                UserRepository(
                    remoteDataSource = AuthRemoteDataSource.getInstance(context),
                    userDao = db.userDao,
                    deviceDao = db.deviceDao
                ).also { repo -> _sInstance = repo }
            }
        }
    }

    fun getCurrentUser(): User? {
        val userEntity = userDao.selectByIsSelect(true)
        if (userEntity == null)
            return null
        val projectID = remoteDataSource.getProjectId()
        val device = deviceDao.selectByProjectId(projectID)
        return User(
            uid = userEntity.uid,
            email = userEntity.email,
            isTrusted = device?.isTrusted == true,
            isAdmin = userEntity.isAdmin
        )
    }

//    fun getCurrentUserAsFlow() = flow {
//        emit(Resources.Loading())
//    }

    fun getUserAsFlow(uid: String?) = callbackFlow<Resources<User>> {
        trySend(Resources.Loading())
        val trustedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val projectID = remoteDataSource.getProjectId()
                val deviceEntity = deviceDao.selectByProjectId(projectID)
                val isTrusted = snapshot.value != null
                if (deviceEntity == null || deviceEntity.isTrusted == isTrusted)
                    return
                val newDeviceEntity = deviceEntity.copy(isTrusted = isTrusted)
                val resInsertDevice = deviceDao.insert(newDeviceEntity)
                if (resInsertDevice > 0) {
                    val userEntity = userDao.selectByID(uid)
                    if (userEntity != null) {
                        val userModel = User(
                            uid = userEntity.uid,
                            email = userEntity.email,
                            isTrusted = isTrusted,
                            isAdmin = userEntity.isAdmin
                        )
                        trySend(Resources.Success(userModel))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        userDao.selectByIDAsFlow(uid).collect { userEntity ->
            val projectID = remoteDataSource.getProjectId()
            val deviceEntity = deviceDao.selectByProjectId(projectID = projectID)
            val trusted = remoteDataSource.queryTrustedDevices(deviceEntity?.code)
            trusted.removeEventListener(trustedListener)
            if (userEntity != null) {
                val isTrusted = deviceEntity?.isTrusted == true
                val userModel = User(
                    uid = userEntity.uid,
                    email = userEntity.email,
                    isTrusted = isTrusted,
                    isAdmin = userEntity.isAdmin
                )
                trySend(Resources.Success(userModel))
                trusted.addValueEventListener(trustedListener)
                return@collect
            }
            close(LoginFirstThrowable())
        }
        awaitClose()
    }.catch { emit(Resources.Failure(it)) }
        .flowOn(Dispatchers.IO)

    fun signOut(uid: String?) = flow<Resources<Unit>> {
        if (uid.isNullOrBlank()) {
            emit(Resources.Failure(LoginFirstThrowable()))
            return@flow
        }
        remoteDataSource.signOut()
        userDao.updateIsSelectByUID(uid, false)
        emit(Resources.Success(Unit))
    }.catch { emit(Resources.Failure(it)) }
        .flowOn(Dispatchers.IO)
}