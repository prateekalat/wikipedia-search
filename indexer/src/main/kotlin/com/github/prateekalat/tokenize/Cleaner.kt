package com.github.prateekalat.tokenize

class Cleaner {

    private val nonAlphabetPattern = "[^A-Za-z ]".toRegex()
    private val linkPattern = "\\[http.*]".toRegex() // Partially cleans links

    fun clean(collection: Collection<String>) : String = clean(collection.joinToString(" "))

    fun clean(text: String) : String {
        var newText = text.replace(linkPattern, "")
        newText = newText.replace("'", "")
        return newText
                .replace(nonAlphabetPattern, " ")
                .trim()
                .toLowerCase()
    }
}