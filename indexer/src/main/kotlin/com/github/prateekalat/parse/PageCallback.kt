package com.github.prateekalat.parse

interface PageCallback {
    fun process(page: Page)
    fun complete()
}