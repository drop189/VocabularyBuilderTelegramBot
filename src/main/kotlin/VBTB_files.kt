import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0
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
            correctAnswersCount = splitLines.getOrNull(2)?.toIntOrNull() ?: 0
        )
        dictionary.add(word)

    }
    dictionary.forEach { println(it) }

    while (true) {
        println("Выберите пункт из меню")
        val userInput = readln().toIntOrNull()
        when (userInput) {
            1 -> {

            }

            2 -> {
                val learnedWords = dictionary.filter { word: Word -> word.correctAnswersCount >= 3 }
                val percentageOfLearnedWords = ((learnedWords.size.toDouble() / dictionary.size) * 100).toInt()

                println("Выучено ${learnedWords.size} из ${dictionary.size} слов | ${percentageOfLearnedWords}%")
            }

            0 -> {
                println("Всего хорошего")
                return
            }

            else -> {
                println("Введен неверный пункт меню")
            }
        }
    }

}