package com.github.prateekalat.parse

import io.reactivex.Observable
import java.io.File
import javax.xml.parsers.SAXParser

class PageParser(private val saxParser: SAXParser) {

    fun getPages(inputFile: File) : Observable<TextParser> = Observable.create {

        saxParser.parse(inputFile, PageHandler(object : PageCallback {

            override fun process(page: Page) {
//                System.out.println("PageParser: ${Thread.currentThread()}")
                it.onNext(TextParser(page))
            }

            override fun complete() { it.onComplete() }

        }))
    }
}