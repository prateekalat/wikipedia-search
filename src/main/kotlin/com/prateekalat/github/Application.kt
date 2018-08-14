package com.prateekalat.github

import com.github.prateekalat.parse.Page
import com.github.prateekalat.parse.PageCallback
import com.github.prateekalat.parse.PageHandler
import com.github.prateekalat.parse.WikiTextParser
import com.github.prateekalat.tokenize.Cleaner
import com.github.prateekalat.tokenize.Tokenizer
import java.io.File
import javax.xml.parsers.SAXParserFactory

class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val parserFactory = SAXParserFactory.newInstance()
            val saxParser = parserFactory.newSAXParser()
            val cleaner = Cleaner()
            val tokenizer = Tokenizer()



            val handler = PageHandler(object : PageCallback {
                override fun process(page: Page) {
                    System.out.println(cleaner.clean(page.title))

                    val wikiTextParser = WikiTextParser(page.wikiText)
                    val cleanedText = cleaner.clean(wikiTextParser.textBody)

                    System.out.println(tokenizer.tokenize(cleanedText))
                }
            })
            saxParser.parse(File("Z:/SearchEngine/wiki-search-small.xml"), handler)
        }
    }
}