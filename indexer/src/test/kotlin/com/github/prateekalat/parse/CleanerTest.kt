package com.github.prateekalat.parse

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class CleanerTest {
    @Test
    fun testAlphabetPattern() {
        val patternNonAlphaNumeric = "[^A-Za-z ]".toRegex()
        System.out.println(" Chlamydia_spp. IgG, IgM & IgA Abs (8006) "
                .replace(patternNonAlphaNumeric, ""))
    }

    @Test
    fun testLinkPattern() {
        val patternLink = "\\[http.*]".toRegex()
        val link = "[http://links.jstor.org/sici?sici=0002-7294(199809)2%3A100%3A3%3C716%3ATMOAAI%3E2.0.CO%3B2-3 The Mis" +
                "representation of Anthropology and its Consequences]"
        assertThat(link.matches(patternLink), `is`(true))

        assertThat(link.replace(patternLink, ""),`is`(""))
    }
}