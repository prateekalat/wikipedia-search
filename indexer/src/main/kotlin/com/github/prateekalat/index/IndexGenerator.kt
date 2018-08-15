package com.github.prateekalat.index

import java.util.*

class IndexGenerator {
    fun generateIndex(pageId: Int, map: TreeMap<String, Int>) : String {
        val builder = StringBuilder()

        for ((word, frequency) in map) {
            builder.append("%s:d%d-%d\n".format(word, pageId, frequency))
        }

        return builder.toString()
    }
}