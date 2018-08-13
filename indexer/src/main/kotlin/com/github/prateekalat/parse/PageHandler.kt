package com.github.prateekalat.parse

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler


class PageHandler(private val callback: PageCallback) : DefaultHandler() {

    private var insideRevision = false
    private var currentPage: Page? = null
    private var currentTag: String? = null

    private var currentWikitext: StringBuilder? = null
    private var currentTitle: StringBuilder? = null
    private var currentID: StringBuilder? = null

    override fun startElement(uri: String?, name: String?, qName: String?, attr: Attributes?) {
        currentTag = qName
        if (qName == "page") {
            currentPage = Page()
            currentWikitext = StringBuilder("")
            currentTitle = StringBuilder("")
            currentID = StringBuilder("")
        }

        if (qName == "revision") {
            insideRevision = true
        }

    }

    override fun endElement(uri: String?, name: String?, qName: String?) {
        if (qName == "revision") {
            insideRevision = false
        }
        if (qName == "page") {
            currentPage?.let {
                it.title = currentTitle.toString().trim()
                it.id = currentID.toString().trim()
                it.wikiText = currentWikitext.toString().trim()
                callback.process(it)
            }
        }
        if (qName == "mediawiki") {
            // TODO hasMoreElements() should now return false
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (currentTag == "title") {
            currentTitle = currentTitle?.append(ch, start, length)
        } else if (currentTag == "id" && !insideRevision) {
            currentID?.append(ch, start, length)
        } else if (currentTag == "text") {
            currentWikitext = currentWikitext?.append(ch, start, length)
        }// Avoids looking at the revision ID, only the first ID should be taken.
    }
}