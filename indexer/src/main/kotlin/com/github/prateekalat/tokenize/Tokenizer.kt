package com.github.prateekalat.tokenize

class Tokenizer {
    private val whitespaceMatcher = "\\s+".toRegex()
    fun tokenize(text: String) = text.split(whitespaceMatcher).toSet()
}