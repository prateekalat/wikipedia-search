package com.github.prateekalat.parse

import org.junit.Test

class WikiParserTest {

    @Test
    fun testParse() {
        val parser = WikiTextParser("")
        System.out.println(parser.redirectText)
    }
}