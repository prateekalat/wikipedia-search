package com.prateekalat.github

import com.github.prateekalat.parse.*
import java.io.File
import javax.xml.parsers.SAXParserFactory

class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val parserFactory = SAXParserFactory.newInstance()
            val saxParser = parserFactory.newSAXParser()



            val handler = PageHandler(object : PageCallback {
                override fun process(page: Page) {
                    System.out.println(page.title)

                    val wikiTextParser = WikiTextParser(page.wikiText)
                    System.out.println(wikiTextParser.links)
                }
            })
            saxParser.parse(File("Z:/SearchEngine/wiki-search-small.xml"), handler)

            val page = handler
        }
    }
}