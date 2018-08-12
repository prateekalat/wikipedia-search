package com.github.prateekalat.parse

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test

class SectionParserTest {

    @Test
    fun regexTest() {
        val delimiter = "[ \n]*=+[a-zA-Z ]+=+[ \n]*".toRegex()
        val string = "Hey \n== Hello there == \nThere"
//        System.out.println(string.matches())
        assertThat(string.split(delimiter), `is`(listOf("Hey", "There")))
    }
}