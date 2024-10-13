package com.lizpostudio.kgoptometrycrm.layer.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.lizpostudio.kgoptometrycrm.layer.data.AuthRepository

class HomeViewModel(
    private val authRepo: AuthRepository
) : ViewModel() {

    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(
                    authRepo = AuthRepository.getInstance(application)
                ) as T
            }
        }
    }

    fun getUser(id: String?) = authRepo.getUser(id)
        .asLiveData(viewModelScope.coroutineContext)

    fun signOut(uid: String?) = authRepo.signOut(uid)
        .asLiveData(viewModelScope.coroutineContext)
}