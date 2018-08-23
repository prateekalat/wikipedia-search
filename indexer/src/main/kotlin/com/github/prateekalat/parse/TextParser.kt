package com.github.prateekalat.parse


import java.util.regex.Matcher
import java.util.regex.Pattern


class TextParser(page: Page) {
    private val text: String
    val title = page.title
    var matcher: Matcher
    var isRedirect = false
        private set
    var redirectText: String? = null
        private set
    var isStub = false
    var isDisambiguationPage = false
    val infoBox: InfoBox by lazy { parseInfoBox() }

    val categories: HashSet<String> by lazy { parseCategories() }

    val links: HashSet<String> by lazy { parseLinks() }
    /**
     * Return only the unformatted text body. Heading markers are omitted.
     * @return the unformatted text body
     */
    val textBody: String
        get() {
            var text = plainText
            text = stripBottomInfo(text, "See also")
            text = stripBottomInfo(text, "Further reading")
            text = stripBottomInfo(text, "References")
            text = stripBottomInfo(text, "Notes")
            text = cleanHeadings(text)
            return text
        }

    // For example: transform match to upper case
    val plainText: String
        get() {
            var text = this.text.replace("&gt;".toRegex(), ">")
            text = text.replace("&lt;".toRegex(), "<")
            text = infoboxCleanupPattern.matcher(text).replaceAll(" ")
            text = commentsCleanupPattern.matcher(text).replaceAll(" ")
            text = stylesPattern.matcher(text).replaceAll(" ")
            text = refCleanupPattern.matcher(text).replaceAll(" ")
            text = text.replace("</?.*?>".toRegex(), " ")
            text = curlyCleanupPattern0.matcher(text).replaceAll(" ")
            text = curlyCleanupPattern1.matcher(text).replaceAll(" ")
            text = cleanupPattern0.matcher(text).replaceAll(" ")

            val m = cleanupPattern1.matcher(text)
            val sb = StringBuffer()
            while (m.find()) {
                val i = m.group().lastIndexOf('|')
                val replacement: String
                replacement = if (i > 0) {
                    m.group(1).substring(i - 1)
                } else {
                    m.group(1)
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement))
            }
            m.appendTail(sb)
            text = sb.toString()

            text = text.replace("'{2,}".toRegex(), "")
            return text.trim { it <= ' ' }
        }

    init {
        text = page.wikiText
        matcher  = stubPattern.matcher(text)
        findRedirect(text)
        isStub = matcher.find()
        matcher = disambiguationPattern.matcher(text)
        isDisambiguationPage = matcher.find()
    }

    /**
     * Check for redirects
     * @param wikiText  the currently parsed page
     */
    private fun findRedirect(wikiText: String) {
        val matcher = redirectPattern.matcher(wikiText)
        if (matcher.find()) {
            isRedirect = true
            if (matcher.groupCount() == 1) {
                redirectText = matcher.group(1)
            }
        }
    }

    private fun parseCategories(): HashSet<String> {
        val pageCats = HashSet<String>()
        val catPattern = Pattern.compile("\\[\\[" + "Category" + ":(.*?)\\]\\]", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
        val matcher = catPattern.matcher(text)
        while (matcher.find()) {
            val temp = matcher.group(1).split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            pageCats.add(temp[0])
        }

        return pageCats
    }

    private fun parseLinks(): HashSet<String> {
        val pageLinks = HashSet<String>()
        val catPattern = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.MULTILINE)
        val matcher = catPattern.matcher(text)
        while (matcher.find()) {
            val temp = matcher.group(1).split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (temp.isEmpty()) {
                continue
            }
            val link = temp[0]
            if (!link.contains(":")) {
                pageLinks.add(link)
            }
        }

        return pageLinks
    }

    /**
     * Strips any content following a specific heading, e.g. "See also", "References", "Notes", etc.
     * Everything following this heading (including the heading) is cut from the text.
     * @param text The wiki page text
     * @param label the heading label to cut
     * @return  the processed wiki text
     */
    private fun stripBottomInfo(text: String, label: String): String {
        var newText = text
        val bottomPattern = Pattern.compile("^=*\\s?$label\\s?=*$", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
        val matcher = bottomPattern.matcher(newText)
        if (matcher.find())
            newText = newText.substring(0, matcher.start())
        return newText
    }

    /**
     * Cleans the surrounding annotations on headings (e.g. "==" or "==="). Leaves the heading word intact.
     * @param text  the wiki text
     * @return  the processed text
     */
    private fun cleanHeadings(text: String): String {
        var newText = text
        val startHeadingPattern = Pattern.compile("^=*", Pattern.MULTILINE)
        val endHeadingPattern = Pattern.compile("=*$", Pattern.MULTILINE)
        newText = startHeadingPattern.matcher(newText).replaceAll("")
        newText = endHeadingPattern.matcher(newText).replaceAll("")
        return newText
    }


    //TODO: ignore brackets in html/xml comments (or better still implement a formal grammar for wiki markup)
    @Throws(WikiTextParserException::class)
    private fun parseInfoBox(): InfoBox {
        val INFOBOX_CONST_STR = "{{Infobox"
        val startPos = text.indexOf(INFOBOX_CONST_STR)
        //if (startPos < 0) return null;
        if (startPos < 0) return InfoBox(null)
        var bracketCount = 2
        var endPos = startPos + INFOBOX_CONST_STR.length
        while (endPos < text.length) {
            when (text[endPos]) {
                '}' -> bracketCount--
                '{' -> bracketCount++
            }
            if (bracketCount == 0) break
            endPos++
        }

        if (bracketCount != 0) {
            throw WikiTextParserException("Malformed Infobox, couldn't match the brackets.")
        }

        var infoBoxText = text.substring(startPos, endPos + 1)
        infoBoxText = stripCite(infoBoxText) // strip clumsy {{cite}} tags
        // strip any html formatting
        infoBoxText = infoBoxText.replace("&gt;".toRegex(), ">")
        infoBoxText = infoBoxText.replace("&lt;".toRegex(), "<")
        infoBoxText = infoBoxText.replace("<ref.*?>.*?</ref>".toRegex(), " ")
        infoBoxText = infoBoxText.replace("</?.*?>".toRegex(), " ")
        return InfoBox(infoBoxText)
    }

    private fun stripCite(text: String): String {
        var newText = text
        val CITE_CONST_STR = "{{cite"
        val startPos = newText.indexOf(CITE_CONST_STR)
        if (startPos < 0) return newText
        var bracketCount = 2
        var endPos = startPos + CITE_CONST_STR.length
        while (endPos < newText.length) {
            when (newText[endPos]) {
                '}' -> bracketCount--
                '{' -> bracketCount++
            }
            if (bracketCount == 0) break
            endPos++
        }
        newText = newText.substring(0, startPos - 1) + newText.substring(endPos)
        return stripCite(newText)
    }

    companion object {
        private val redirectPattern = Pattern.compile("#" + "#REDIRECT" + "\\s*\\[\\[(.*?)\\]\\]", Pattern.CASE_INSENSITIVE)
        private val stubPattern = Pattern.compile("\\-" + "stub" + "\\}\\}", Pattern.CASE_INSENSITIVE)
        private val disambiguationPattern = Pattern.compile("\\{\\{" + "disambig" + "\\}\\}", Pattern.CASE_INSENSITIVE)

        private val stylesPattern = Pattern.compile("\\{\\|.*?\\|\\}$", Pattern.MULTILINE or Pattern.DOTALL)
        private val infoboxCleanupPattern = Pattern.compile("\\{\\{infobox.*?\\}\\}$", Pattern.MULTILINE or Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
        private val curlyCleanupPattern0 = Pattern.compile("^\\{\\{.*?\\}\\}$", Pattern.MULTILINE or Pattern.DOTALL)
        private val curlyCleanupPattern1 = Pattern.compile("\\{\\{.*?\\}\\}", Pattern.MULTILINE or Pattern.DOTALL)
        private val cleanupPattern0 = Pattern.compile("^\\[\\[.*?:.*?\\]\\]$", Pattern.MULTILINE or Pattern.DOTALL)
        private val cleanupPattern1 = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.MULTILINE or Pattern.DOTALL)
        private val refCleanupPattern = Pattern.compile("<ref>.*?</ref>", Pattern.MULTILINE or Pattern.DOTALL)
        private val commentsCleanupPattern = Pattern.compile("<!--.*?-->", Pattern.MULTILINE or Pattern.DOTALL)
    }
}