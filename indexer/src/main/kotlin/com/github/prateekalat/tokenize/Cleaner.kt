package com.github.prateekalat.tokenize

import com.github.prateekalat.parse.TextParser
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class Cleaner(val pages: Observable<TextParser>) {

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

    fun getCleanedStrings() : Observable<List<String>> =
        pages.observeOn(Schedulers.computation())
            .map {
//                System.out.println("Cleaner: ${Thread.currentThread()}")

                listOf(
                        clean(it.textBody),
                        clean(it.categories),
                        clean(it.title),
                        clean(it.infoBox.dumpRaw() ?: ""),
                        clean(it.links)
                )
            }
}