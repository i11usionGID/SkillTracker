package com.i11usion.feature.main.presentation.user.create.mapper

import AppError
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText

fun AppError.toCreateUserUiError(): UiError {
    return when (this) {
        AppError.Network -> UiError.SnackBar(UiText.Res(com.i11usion.core.ui.R.string.no_internet))

        AppError.ServerError -> UiError.FullScreen(
            ErrorScreenModel(
                iconResId = com.i11usion.core.ui.R.drawable.ic_cloud_alert,
                cdResId = com.i11usion.core.ui.R.string.cd_server_error,
                text = UiText.Res(com.i11usion.core.ui.R.string.server_error_try_later),
                showBack = true
            )
        )

        is AppError.Unauthorized -> UiError.FullScreen(
            ErrorScreenModel(
                iconResId = com.i11usion.core.ui.R.drawable.ic_not_found,
                cdResId = com.i11usion.core.ui.R.string.cd_not_found,
                text = UiText.Dynamic("Не удалось найти ресурс. Обратитесь к администратору."),
                showBack = true
            )
        )

        is AppError.Validation -> UiError.SnackBar(UiText.Dynamic(message))

        AppError.NotFound -> UiError.SnackBar(UiText.Dynamic("Ничего не найдено"))
        AppError.Unknown -> UiError.SnackBar(UiText.Dynamic("Не удалось создать пользователя"))
    }
}