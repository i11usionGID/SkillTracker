package com.i11usion.core.ui.formatter

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dashboardDateFormatter: DateTimeFormatter =
    DateTimeFormatter
        .ofPattern("d MMMM yyyy 'г.'", Locale.forLanguageTag("ru-RU"))
        .withZone(ZoneId.systemDefault())

fun formatDashboardDate(instant: Instant?): String =
    instant?.let { dashboardDateFormatter.format(it) } ?: ""