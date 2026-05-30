package com.spotlyric.app.domain.error

/**
 * Enumeration of all possible error types that can occur in the application.
 * Used to classify errors for appropriate handling and user-facing messaging.
 */
enum class ErrorCode {
    // Network errors
    NETWORK_UNREACHABLE,
    NETWORK_TIMEOUT,
    CONNECTION_RESET,
    
    // HTTP errors
    HTTP_401_UNAUTHORIZED,
    HTTP_403_FORBIDDEN,
    HTTP_404_NOT_FOUND,
    HTTP_429_RATE_LIMITED,
    HTTP_500_SERVER_ERROR,
    HTTP_503_SERVICE_UNAVAILABLE,
    HTTP_UNKNOWN,
    
    // Auth errors
    AUTH_EXPIRED_TOKEN,
    AUTH_INVALID_CREDENTIALS,
    AUTH_TOKEN_REFRESH_FAILED,
    AUTH_PKCE_FAILED,
    
    // API response errors
    INVALID_JSON_RESPONSE,
    MISSING_REQUIRED_FIELD,
    INVALID_DATA_FORMAT,
    
    // Gemini/AI errors
    GEMINI_API_QUOTA_EXCEEDED,
    GEMINI_API_ERROR,
    GEMINI_SAFETY_FILTER_TRIGGERED,
    EXTRACTION_CONFIDENCE_TOO_LOW,
    
    // Database errors
    DATABASE_ERROR,
    DATABASE_CONSTRAINT_VIOLATION,
    
    // Storage errors
    DATASTORE_ERROR,
    ENCRYPTION_ERROR,
    
    // File/Content errors
    FILE_NOT_FOUND,
    CONTENT_SIZE_EXCEEDED,
    HTML_PARSING_ERROR,
    
    // Generic/Unknown
    UNKNOWN_ERROR,
    OPERATION_CANCELLED
}
