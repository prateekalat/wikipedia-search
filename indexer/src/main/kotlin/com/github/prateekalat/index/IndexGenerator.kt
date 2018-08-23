package com.github.prateekalat.index

import com.github.prateekalat.tokenize.Stemmer
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicInteger

class IndexGenerator(val stemmer: Stemmer) {

    fun generateIndex(): Observable<String> {

        val builder = StringBuilder()
        return stemmer.getStemmedTokens()
                .map {

//                    System.out.println("IndexGenerator: ${Thread.currentThread()}")

                    builder.setLength(0)

                    val pageId = atomicPageId.getAndIncrement()

                    for ((word, frequency) in it[0]) {
                        builder.append("%s:d%d-%d\n".format(word, pageId, frequency))
                    }

                    for ((word, frequency) in it[1]) {
                        builder.append("%s-c:d%d-%d\n".format(word, pageId, frequency))
                    }

                    for ((word, frequency) in it[2]) {
                        builder.append("%s-t:d%d-%d\n".format(word, pageId, frequency))
                    }

                    for ((word, frequency) in it[3]) {
                        builder.append("%s-i:d%d-%d\n".format(word, pageId, frequency))
                    }

                    for ((word, frequency) in it[4]) {
                        builder.append("%s-l:d%d-%d\n".format(word, pageId, frequency))
                    }

                    builder.toString()
                }
    }

    companion object {
        var atomicPageId = AtomicInteger(1)
    }
}