package com.github.prateekalat.index

import java.util.*

class IndexGenerator {
    fun generateIndex(
            pageId: Int,
            textTokens: TreeMap<String, Int>,
            categoryTokens: TreeMap<String, Int>,
            titleTokens: TreeMap<String, Int>,
            infoBoxTokens: TreeMap<String, Int>,
            linkTokens: TreeMap<String, Int>
    ) : String {
        val builder = StringBuilder()

        for ((word, frequency) in textTokens) {
            builder.append("%s:d%d-%d\n".format(word, pageId, frequency))
        }

        for ((word, frequency) in categoryTokens) {
            builder.append("%s-c:d%d-%d\n".format(word, pageId, frequency))
        }

        for ((word, frequency) in titleTokens) {
            builder.append("%s-t:d%d-%d\n".format(word, pageId, frequency))
        }

        for ((word, frequency) in infoBoxTokens) {
            builder.append("%s-i:d%d-%d\n".format(word, pageId, frequency))
        }

        for ((word, frequency) in linkTokens) {
            builder.append("%s-l:d%d-%d\n".format(word, pageId, frequency))
        }

        return builder.toString()
    }
}