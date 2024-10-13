package com.lizpostudio.kgoptometrycrm.layer.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.lizpostudio.kgoptometrycrm.layer.data.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class HomeViewModel(
    private val userRepo: UserRepository
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
                    userRepo = UserRepository.getInstance(application)
                ) as T
            }
        }
    }

    private val _userState = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val user = _userState.flatMapLatest { uid ->
        userRepo.getUserAsFlow(uid)
    }.asLiveData(viewModelScope.coroutineContext)

    fun setUser(uid: String?) {
        _userState.value = uid
    }

    fun signOut(uid: String?) = userRepo.signOut(uid)
        .asLiveData(viewModelScope.coroutineContext)
}