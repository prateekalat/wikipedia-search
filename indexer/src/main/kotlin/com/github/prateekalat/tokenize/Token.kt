package com.github.prateekalat.tokenize

enum class TokenType {
    BODY, CATEGORY, TITLE, INFOBOX, LINK,
}

data class Token(
        val text: String,
        val type: TokenType
)