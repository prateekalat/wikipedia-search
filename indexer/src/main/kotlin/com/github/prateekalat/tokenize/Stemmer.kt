package com.github.prateekalat.tokenize

import org.tartarus.snowball.Stemmer
import java.util.*

class Stemmer(val tokenizer: Tokenizer) {
    private val internalStemmer = Stemmer()

    fun stemTreeMap(inputMap: TreeMap<String, Int>) : TreeMap<String, Int> {

        val outputMap: TreeMap<String, Int> = TreeMap()

        for ((key, value) in inputMap) {

            internalStemmer.current = key
            internalStemmer.stem()

            val stemmedWord = internalStemmer.current

            if (StopWordsUtil.stopWords.contains(stemmedWord)) continue

            val currentMapValue = outputMap[stemmedWord]
            if (currentMapValue == null) {
                outputMap[stemmedWord] = value
            } else {
                outputMap[stemmedWord] = currentMapValue + value
            }
        }

        return outputMap
    }

    fun getStemmedTokens() = tokenizer.getListOfTokenSets().map { it ->
//        System.out.println("Stemmer: ${Thread.currentThread()}")

        it.map { stemTreeMap(it) }
    }
}