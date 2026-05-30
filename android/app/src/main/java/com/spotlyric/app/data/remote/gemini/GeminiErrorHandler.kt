package com.spotlyric.app.data.remote.gemini

import com.spotlyric.app.domain.error.ErrorCode
import com.spotlyric.app.domain.error.LyricsResult
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Handles mapping of exceptions to typed ErrorCode and LyricsResult.
 * Replaces broad catch(Exception) with specific, typed error handling.
 */
object GeminiErrorHandler {
    
    /**
     * Map exception to ErrorCode with descriptive message
     */
    fun mapException(exception: Throwable): Pair<ErrorCode, String> = when (exception) {
        is HttpException -> when (exception.code()) {
            401 -> ErrorCode.HTTP_401_UNAUTHORIZED to "API key invalid or expired"
            403 -> ErrorCode.HTTP_403_FORBIDDEN to "Access forbidden for this API"
            404 -> ErrorCode.HTTP_404_NOT_FOUND to "API endpoint not found"
            429 -> ErrorCode.HTTP_429_RATE_LIMITED to "API rate limit exceeded"
            500 -> ErrorCode.HTTP_500_SERVER_ERROR to "Gemini server error"
            503 -> ErrorCode.HTTP_503_SERVICE_UNAVAILABLE to "Gemini service unavailable"
            else -> ErrorCode.HTTP_UNKNOWN to "HTTP error ${exception.code()}"
        }
        is SocketTimeoutException -> ErrorCode.NETWORK_TIMEOUT to "Request timeout"
        is IOException -> when {
            exception.message?.contains("Network") == true -> 
                ErrorCode.NETWORK_UNREACHABLE to "Network unreachable"
            exception.message?.contains("reset") == true ->
                ErrorCode.CONNECTION_RESET to "Connection reset by peer"
            else -> ErrorCode.NETWORK_UNREACHABLE to "Network error: ${exception.message}"
        }
        else -> ErrorCode.UNKNOWN_ERROR to "Unexpected error: ${exception.message}"
    }

    /**
     * Wrap JSON parsing or validation errors
     */
    fun jsonParseError(message: String, cause: Throwable? = null): LyricsResult.Error<String> {
        return LyricsResult.Error(
            code = ErrorCode.INVALID_JSON_RESPONSE,
            message = "Failed to parse Gemini response: $message",
            cause = cause
        )
    }

    /**
     * Check if confidence score is too low for reliability
     */
    fun isConfidenceTooLow(score: Float, threshold: Float = 0.3f): Boolean = score < threshold

    /**
     * Validate response fields exist
     */
    fun validateFields(vararg fields: String?): Boolean = fields.all { !it.isNullOrBlank() }
}
