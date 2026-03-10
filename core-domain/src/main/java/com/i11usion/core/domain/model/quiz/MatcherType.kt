package com.i11usion.core.domain.model.quiz

enum class MatcherType {
    EQUALS,
    CONTAINS,
    ICONTAINS;

    companion object {

        fun fromApi(value: String): MatcherType = when (value.lowercase()) {
            "equals" -> EQUALS
            "contains" -> CONTAINS
            "icontains" -> ICONTAINS
            else -> error("Unknown MatcherType: $value")
        }

        fun toApi(type: MatcherType): String = when (type) {
            EQUALS -> "equals"
            CONTAINS -> "contains"
            ICONTAINS -> "icontains"
        }
    }
}