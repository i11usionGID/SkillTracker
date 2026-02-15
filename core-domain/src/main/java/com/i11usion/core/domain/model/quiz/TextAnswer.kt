package com.i11usion.core.domain.model.quiz

data class TextAnswer(
    val id: Int,
    val matcherType: MatcherType = MatcherType.ICONTAINS,
    val value: String,
)