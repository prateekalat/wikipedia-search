package com.prateekalat.github

import com.github.prateekalat.parse.Page
import com.github.prateekalat.parse.PageCallback
import com.github.prateekalat.parse.PageHandler
import com.github.prateekalat.parse.WikiTextParser
import com.github.prateekalat.tokenize.Cleaner
import com.github.prateekalat.tokenize.Tokenizer
import org.tartarus.snowball.Stemmer
import java.io.File
import javax.xml.parsers.SAXParserFactory

class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val startTime = System.currentTimeMillis()


            val parserFactory = SAXParserFactory.newInstance()
            val saxParser = parserFactory.newSAXParser()
            val cleaner = Cleaner()
            val tokenizer = Tokenizer()
            val stemmer = Stemmer()

            val handler = PageHandler(object : PageCallback {
                override fun process(page: Page) {
//                    System.out.println(cleaner.clean(page.title))

                    val wikiTextParser = WikiTextParser(page.wikiText)
                    val cleanedText = cleaner.clean(wikiTextParser.textBody)

                    val tokens = tokenizer.tokenize(cleanedText)
                    for (token in tokens) {
                        stemmer.current = token
                        stemmer.stem()
//                        System.out.println(stemmer.current)
                    }
                }
            })
            saxParser.parse(File("Z:/SearchEngine/wiki-search-small.xml"), handler)

            val currentTime = System.currentTimeMillis()
            System.out.println("%d seconds".format((currentTime - startTime) / 1000))
        }
    }
}