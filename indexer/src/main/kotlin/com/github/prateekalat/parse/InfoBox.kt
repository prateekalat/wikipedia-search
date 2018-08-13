package com.github.prateekalat.parse


class InfoBox internal constructor(infoBoxWikiText: String?) {
    private var infoBoxWikiText: String? = null
    val isEmpty: Boolean
        get() = infoBoxWikiText!!.isEmpty()

    init {
        //to to be the following line
        //this.infoBoxWikiText = infoBoxWikiText;
        if (infoBoxWikiText != null) {
            this.infoBoxWikiText = infoBoxWikiText
        } else {
            //set infobox text to empty string
            this.infoBoxWikiText = String()
        }
    }

    fun dumpRaw(): String? {
        return infoBoxWikiText
    }
}
