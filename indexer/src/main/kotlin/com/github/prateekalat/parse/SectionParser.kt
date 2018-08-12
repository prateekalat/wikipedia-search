package com.github.prateekalat.parse

class SectionParser {
    private val delimiter = "[ \n]*=+[a-zA-Z0-9 /'\",.()-]+=+[ \n]".toRegex() // REGEX of the form '=== Heading ==='

    fun parse(text: String) = text.split(delimiter)
}