package com.prateekalat.github

import com.github.prateekalat.index.IndexGenerator
import com.github.prateekalat.parse.Page
import com.github.prateekalat.parse.PageCallback
import com.github.prateekalat.parse.PageHandler
import com.github.prateekalat.parse.TextParser
import com.github.prateekalat.tokenize.Cleaner
import com.github.prateekalat.tokenize.Stemmer
import com.github.prateekalat.tokenize.Tokenizer
import com.google.code.externalsorting.ExternalSort.*
import java.io.File
import javax.xml.parsers.SAXParserFactory

class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val startTime = System.currentTimeMillis()

            var pageId = 1

            val unsortedIndexFile = File("Z:/SearchEngine/out.txt").apply { if (exists()) delete() }
            val writer = unsortedIndexFile.bufferedWriter()

            val parserFactory = SAXParserFactory.newInstance()
            val saxParser = parserFactory.newSAXParser()
            val cleaner = Cleaner()
            val tokenizer = Tokenizer()
            val stemmer = Stemmer()
            val indexGenerator = IndexGenerator()

            val handler = PageHandler(object : PageCallback {
                override fun process(page: Page) {

                    val wikiTextParser = TextParser(page.wikiText)
                    val cleanedText = cleaner.clean(wikiTextParser.textBody)

                    val tokens = tokenizer.tokenize(cleanedText)
                    val stemmedTokens = stemmer.stemTreeMap(tokens)

                    val index = indexGenerator.generateIndex(pageId, stemmedTokens)

                    writer.append(index)

                    pageId++
                }
            })
            saxParser.parse(File("Z:/SearchEngine/wiki-search-small.xml"), handler)

            val comparator: Comparator<String> = Comparator { o1, o2 ->
                o1.split(":")[0].compareTo(o2.split(":")[0])
            }

            val listOfFiles = sortInBatch(unsortedIndexFile, comparator,
                    File("Z:/SearchEngine/"), true, 0)

            mergeSortedFiles(listOfFiles, File("Z:/SearchEngine/index.txt"), comparator, true)

            val currentTime = System.currentTimeMillis()
            System.out.println("%d seconds".format((currentTime - startTime) / 1000))

            writer.close()
        }
    }
}