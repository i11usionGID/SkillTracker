sealed interface AppError {

    data class Unauthorized(
        val type: UnauthorizedType,
        val detail: String? = null
    ) : AppError

    enum class UnauthorizedType {
        InvalidToken,
        InvalidCredentials,
        Unknown
    }

    data object NotFound : AppError
    data object ServerError : AppError
    data object Network : AppError
    data class Validation(val message: String) : AppError
    data object Unknown : AppError
}