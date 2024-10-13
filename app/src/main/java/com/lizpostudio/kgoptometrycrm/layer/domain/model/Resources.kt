package com.lizpostudio.kgoptometrycrm.layer.domain.model

import java.util.concurrent.atomic.AtomicLong

interface Resources<out O> {

    data class Loading(
        val progress: Progress? = null
    ) : Resources<Nothing> {

        class Progress(private val count: AtomicLong, private val length: Long) {
            fun getCount() = count.get()
            fun getLength() = length
        }
    }

    data class Success<T>(
        val value: T
    ) : Resources<T>

    data class Failure(
        val value: Throwable
    ) : Resources<Nothing>
}