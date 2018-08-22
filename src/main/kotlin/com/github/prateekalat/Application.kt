package com.github.prateekalat

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

fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()

    args.forEach { System.out.println(it) }

    var pageId = 1

    if (args.size < 2) {
        System.err.println("Too few arguments. First argument is input directory. Second is output directory.")
        System.exit(1)
    }

    val removeTrailingSlash = { it: Char -> it == '/' }

    val inputDirectory = args[0].dropLastWhile(removeTrailingSlash)
    val outputDirectory = args[1].dropLastWhile(removeTrailingSlash)

    val unsortedIndexFile = File("$outputDirectory/unsorted_index.txt").apply { deleteOnExit() }
    val writer = unsortedIndexFile.bufferedWriter()

    val parserFactory = SAXParserFactory.newInstance()
    val saxParser = parserFactory.newSAXParser()
    val cleaner = Cleaner()
    val tokenizer = Tokenizer()
    val stemmer = Stemmer()
    val indexGenerator = IndexGenerator()

    val handler = PageHandler(object : PageCallback {
        override fun process(page: Page) {

            val textParser = TextParser(page.wikiText)

            val cleanedText = cleaner.clean(textParser.textBody)
            val cleanedCategories = cleaner.clean(textParser.categories)
            val cleanedTitle = cleaner.clean(page.title)
            val cleanedInfoBox = cleaner.clean(textParser.infoBox.dumpRaw() ?: "")
            val cleanedLinks = cleaner.clean(textParser.links)

            val textTokens = tokenizer.tokenize(cleanedText)
            val categoryTokens = tokenizer.tokenize(cleanedCategories)
            val titleTokens = tokenizer.tokenize(cleanedTitle)
            val infoBoxTokens = tokenizer.tokenize(cleanedInfoBox)
            val linkTokens = tokenizer.tokenize(cleanedLinks)

            val stemmedTextTokens = stemmer.stemTreeMap(textTokens)
            val stemmedCategoryTokens = stemmer.stemTreeMap(categoryTokens)
            val stemmedTitleTokens = stemmer.stemTreeMap(titleTokens)
            val stemmedInfoBoxTokens = stemmer.stemTreeMap(infoBoxTokens)
            val stemmedLinkTokens = stemmer.stemTreeMap(linkTokens)

            val index = indexGenerator.generateIndex(
                    pageId,
                    stemmedTextTokens,
                    stemmedCategoryTokens,
                    stemmedTitleTokens,
                    stemmedInfoBoxTokens,
                    stemmedLinkTokens
            )

            writer.append(index)

            pageId++
        }
    })

    saxParser.parse(File("$inputDirectory/wiki-search-small.xml"), handler)

    val comparator: Comparator<String> = Comparator { o1, o2 ->
        o1.split(":")[0].compareTo(o2.split(":")[0])
    }

    val listOfFiles = sortInBatch(unsortedIndexFile, comparator, File(outputDirectory))

    mergeSortedFiles(listOfFiles, File("$outputDirectory/index.txt"), comparator)

    val currentTime = System.currentTimeMillis()
    System.out.println("%d seconds".format((currentTime - startTime) / 1000))

    writer.close()
}
