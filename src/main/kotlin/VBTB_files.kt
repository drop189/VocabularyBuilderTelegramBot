package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: String = "0"
)

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    for (line in lines) {
        val splitLines = line.split("|")
        val word = Word(
            original = splitLines[0],
            translate = splitLines[1],
            correctAnswersCount = splitLines.getOrNull(2) ?: "0"
        )
        dictionary.add(word)

    }
    dictionary.forEach { println(it) }

}