package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.txt")

    val messages = wordsFile.readLines()
    for (message in messages) {
        println(message)
    }
}
