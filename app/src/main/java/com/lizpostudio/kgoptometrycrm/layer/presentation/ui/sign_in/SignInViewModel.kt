package com.lizpostudio.kgoptometrycrm.layer.presentation.ui.sign_in

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.lizpostudio.kgoptometrycrm.layer.data.AuthRepository
import com.lizpostudio.kgoptometrycrm.layer.domain.model.ConfigModel

class SignInViewModel(
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
                return SignInViewModel(
                    authRepo = AuthRepository.getInstance(application)
                ) as T
            }
        }
    }

    fun signIn(email: String, password: String) =
        authRepo.signIn(email, password).asLiveData(viewModelScope.coroutineContext)

    fun changeFirebaseConfig(context: Context, config: ConfigModel) =
        authRepo.changeFirebaseConfig(context, config)

    fun setDefaultFirebaseConfig(context: Context) =
        authRepo.setDefaultFirebaseConfig(context)
}