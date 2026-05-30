package com.spotlyric.app.domain.error

/**
 * Sealed class representing the result of an operation that can succeed or fail.
 * Replaces broad try-catch blocks with type-safe error handling.
 *
 * Usage:
 *   when (val result = someOperation()) {
 *       is LyricsResult.Success -> handleSuccess(result.data)
 *       is LyricsResult.Error -> handleError(result.code, result.message)
 *   }
 */
sealed class LyricsResult<out T> {
    data class Success<T>(val data: T) : LyricsResult<T>()
    
    data class Error<T>(
        val code: ErrorCode,
        val message: String,
        val cause: Throwable? = null
    ) : LyricsResult<T>()

    // Convenience methods
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> cause
    }

    fun fold(
        onSuccess: (T) -> Unit,
        onError: (code: ErrorCode, message: String, cause: Throwable?) -> Unit
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(code, message, cause)
        }
    }

    inline fun <R> map(transform: (T) -> R): LyricsResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(code, message, cause)
    }
}
