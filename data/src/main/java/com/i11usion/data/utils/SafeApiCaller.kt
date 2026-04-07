package com.i11usion.data.utils

import AppError
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.repository.NetworkErrorHandler
import okhttp3.Request
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class SafeApiCaller @Inject constructor(
    private val errorHandler: NetworkErrorHandler
) {

    suspend fun <T> call(block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (e: Exception) {

            when (e) {
                is CancellationException -> throw e

                is HttpException -> {
                    val code = e.code()

                    val rawBody = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                    val detail = extractDetail(rawBody)

                    when (code) {
                        400 -> AppResult.Failure(
                            AppError.Validation(
                                message = detail?.takeIf { it.isNotBlank() }
                                    ?: "Попробуйте удалить все, что находится внутри"
                            )
                        )

                        401 -> {
                            val request = e.response()?.raw()?.request
                            val unauthorizedType = resolveUnauthorizedType(detail, request)

                            if (unauthorizedType == AppError.UnauthorizedType.InvalidToken) {
                                errorHandler.onUnauthorized()
                            }

                            AppResult.Failure(
                                AppError.Unauthorized(
                                    type = unauthorizedType,
                                    detail = detail
                                )
                            )
                        }

                        404 -> AppResult.Failure(AppError.NotFound)

                        422 -> AppResult.Failure(
                            AppError.Validation(
                                message = detail?.takeIf { it.isNotBlank() }
                                    ?: "Проверьте введённые данные"
                            )
                        )

                        429 -> AppResult.Failure(
                            AppError.Validation(
                                message = "Для этого теста есть ограничение на перепрохождение. " +
                                        "Следующая попытка сейчас недоступна."
                            )
                        )

                        in 500..599 -> AppResult.Failure(AppError.ServerError)
                        else -> AppResult.Failure(AppError.Unknown)
                    }
                }

                is IOException -> AppResult.Failure(AppError.Network)
                else -> AppResult.Failure(AppError.Unknown)
            }
        }
    }

    private fun resolveUnauthorizedType(
        detail: String?,
        request: Request?
    ): AppError.UnauthorizedType {
        val hasAuthHeader = !request?.header("Authorization").isNullOrBlank()
        val normalized = detail?.trim()?.lowercase()

        return when {
            normalized == "invalid token" -> AppError.UnauthorizedType.InvalidToken
            normalized == "invalid credentials" -> AppError.UnauthorizedType.InvalidCredentials

            hasAuthHeader -> AppError.UnauthorizedType.InvalidToken

            else -> AppError.UnauthorizedType.Unknown
        }
    }

    private fun extractDetail(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) return null
        val body = rawBody.trim()

        return runCatching {
            if (body.startsWith("{")) {
                JSONObject(body).optString("detail").takeIf { it.isNotBlank() } ?: body
            } else {
                body
            }
        }.getOrElse { body }
    }
}