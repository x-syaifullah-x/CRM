package com.lizpostudio.kgoptometrycrm.layer.presentation.providers

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.lizpostudio.kgoptometrycrm.BuildConfig
import java.io.File

class MyFileProvider : FileProvider() {

    companion object {

        private const val APPLICATION_ID = BuildConfig.APPLICATION_ID
        private const val FILE_PROVIDER_AUTHORITIES = "$APPLICATION_ID.FILE_PROVIDER"

        fun getUriForFile(context: Context, file: File): Uri {
            return getUriForFile(
                context, FILE_PROVIDER_AUTHORITIES, file
            )
        }
    }
}