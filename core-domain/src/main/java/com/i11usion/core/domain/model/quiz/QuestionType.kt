package com.i11usion.core.domain.model.quiz

enum class QuestionType {
    SINGLE,
    MULTIPLE,
    TEXT;

    companion object {
        fun fromApi(value: String): QuestionType = when (value.lowercase()) {
            "single" -> SINGLE
            "multiple" -> MULTIPLE
            "text" -> TEXT
            else -> error("Unknown QuestionType: $value")
        }

        fun toApi(type: QuestionType): String = when (type) {
            SINGLE -> "single"
            MULTIPLE -> "multiple"
            TEXT -> "text"
        }
    }
}

