package com.prateekalat.github

import com.github.prateekalat.parse.PageHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val handler = PageHandler()
            val parserFactory = SAXParserFactory.newInstance()
            val parser = parserFactory.newSAXParser()
            parser.parse(File("Z:/SearchEngine/wiki-search-small.xml"), handler)

            handler.pages.forEach { System.out.println(it) }
        }
    }
}