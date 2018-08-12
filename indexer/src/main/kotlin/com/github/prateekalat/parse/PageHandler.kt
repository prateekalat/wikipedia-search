package com.github.prateekalat.parse

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class PageHandler : DefaultHandler() {

    val pages: ArrayList<Page> = ArrayList()

    private val sectionParser = SectionParser()

    private var pageNumber = 0

    private var title = ""

    private var text = ""

    private var content = StringBuilder()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        when (qName) {
            "page" -> content = StringBuilder()
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        when (qName) {
            "page" -> {
                pages.add(Page(pageNumber, title, sectionParser.parse(text)))
                pageNumber++
            }
            "title" -> title = content.toString()
            "text" -> text = content.toString()
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (ch != null) {
            content.append(String(ch.copyOfRange(start, start + length)).trim())
        }
    }
}