package com.github.prateekalat.tokenize

import java.util.*

class Tokenizer {
    private val whitespaceMatcher = "\\s+".toRegex()

    private fun List<String>.toTreeMap() : TreeMap<String, Int> {
        val map = TreeMap<String, Int>()
        for (item in this) {

            if (StopWordsUtil.stopWords.contains(item)) continue

            if (map.containsKey(item)) {
                val itemValue = map[item]
                // If not null, then increment
                if (itemValue != null) {
                    map[item] = itemValue + 1
                } else {
                    map[item] = 1
                }
            } else {
                // First entry.
                map[item] = 1
            }
        }

        return map
    }

    fun tokenize(text: String) = text
            .split(whitespaceMatcher)
            .toTreeMap()
}