package com.i11usion.core.ui.formatter

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter
        .ofPattern("d MMMM yyyy 'г.'", Locale.forLanguageTag("ru-RU"))
        .withZone(ZoneId.systemDefault())

fun formatInstantToStringDataNullable(instant: Instant?): String =
    instant?.let { dateFormatter.format(it) } ?: ""

fun formatInstantToStringData(instant: Instant): String =
    dateFormatter.format(instant)