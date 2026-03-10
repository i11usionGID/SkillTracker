package com.i11usion.core.domain.helpers

import com.i11usion.core.domain.model.network.AppResult

inline fun <A, B, R> AppResult<A>.zip(
    other: AppResult<B>,
    transform: (A, B) -> R
): AppResult<R> = when (this) {
    is AppResult.Success -> when (other) {
        is AppResult.Success -> AppResult.Success(transform(this.data, other.data))
        is AppResult.Failure -> other
    }
    is AppResult.Failure -> this
}

inline fun <A, B, C, D, R> AppResult<A>.zip4(
    b: AppResult<B>,
    c: AppResult<C>,
    d: AppResult<D>,
    transform: (A, B, C, D) -> R
): AppResult<R> = when (this) {
    is AppResult.Failure -> this
    is AppResult.Success -> when (b) {
        is AppResult.Failure -> b
        is AppResult.Success -> when (c) {
            is AppResult.Failure -> c
            is AppResult.Success -> when (d) {
                is AppResult.Failure -> d
                is AppResult.Success -> AppResult.Success(transform(this.data, b.data, c.data, d.data))
            }
        }
    }
}