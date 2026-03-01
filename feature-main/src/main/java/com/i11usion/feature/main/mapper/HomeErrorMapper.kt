package com.i11usion.feature.main.mapper

import AppError
import com.i11usion.core.ui.R
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText

fun AppError.toHomeUiError(): UiError {
    return when (this) {
        AppError.Network -> UiError.SnackBar(UiText.Res(R.string.no_internet))

        AppError.ServerError ->
            UiError.FullScreen(
                ErrorScreenModel(
                    iconResId = R.drawable.ic_cloud_alert,
                    cdResId = R.string.cd_server_error,
                    text = UiText.Res(R.string.server_error_try_later),
                    showBack = true
                )
            )

        AppError.NotFound ->
            UiError.FullScreen(
                ErrorScreenModel(
                    iconResId = R.drawable.ic_not_found,
                    cdResId = R.string.cd_not_found,
                    text = UiText.Dynamic("Не удалось найти ресурс. Обратитесь к администратору."),
                    showBack = true
                )
            )

        is AppError.Validation -> UiError.SnackBar(
            UiText.Dynamic(message)
        )

        is AppError.Unauthorized -> when (type) {
            AppError.UnauthorizedType.InvalidCredentials ->
                UiError.SnackBar(UiText.Dynamic("Неверный логин или пароль"))

            AppError.UnauthorizedType.InvalidToken ->
                UiError.SnackBar(UiText.Dynamic("Сессия истекла. Войдите снова"))

            AppError.UnauthorizedType.Unknown ->
                UiError.SnackBar(UiText.Dynamic(detail ?: "Ошибка авторизации"))
        }

        AppError.Unknown ->
            UiError.FullScreen(
                ErrorScreenModel(
                    iconResId = R.drawable.ic_fail,
                    cdResId = R.string.cd_unknown_error,
                    text = UiText.Res(R.string.unknown_error),
                    showBack = true
                )
            )
    }
}