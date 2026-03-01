package com.i11usion.core.ui.helpers

import android.content.Context
import com.i11usion.core.ui.model.UiText

fun UiText.asString(context: Context): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Res -> context.getString(id)
}