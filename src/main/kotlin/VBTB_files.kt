import java.io.File

const val REQUIRED_CORRECT_ANSWERS = 3
const val NUMBER_OF_ANSWERS = 4
const val INVALID_GUESS = -1
const val INDEX_CORRECTION = 1

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
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

    while (true) {
        println(
            "Меню: \n" +
                    "1 – Учить слова\n" +
                    "2 – Статистика \n" +
                    "0 – Выход"
        )
        val userInput = readln().toIntOrNull()
        when (userInput) {
            1 -> {
                while (true) {
                    val unlearnedWords =
                        dictionary.filter { it.correctAnswersCount < REQUIRED_CORRECT_ANSWERS }

                    if (unlearnedWords.isEmpty()) {
                        println("Поздравляю, вы выучили все слова")
                        return
                    } else {

                        val answerOptions: List<Word>
                        val correctWord: Word

                        if (unlearnedWords.size < NUMBER_OF_ANSWERS) {

                            val learnedWords =
                                dictionary.filter { it.correctAnswersCount >= REQUIRED_CORRECT_ANSWERS }

                            val listOfWords =
                                unlearnedWords + learnedWords.take(NUMBER_OF_ANSWERS - unlearnedWords.size)

                            answerOptions = listOfWords.shuffled().take(NUMBER_OF_ANSWERS)
                            correctWord = unlearnedWords.random()
                        } else {

                            answerOptions = unlearnedWords.shuffled().take(NUMBER_OF_ANSWERS)
                            correctWord = answerOptions.random()
                        }

                        println("\t${correctWord.original}")
                        answerOptions.forEachIndexed { index, word -> println("${index + INDEX_CORRECTION}. ${word.translate}") }
                        println("0. Главное меню")

                        val userInputGuess = readln().toIntOrNull() ?: INVALID_GUESS
                        val numberOfCorrectWord = answerOptions.indexOf(correctWord) + INDEX_CORRECTION

                        when (userInputGuess) {
                            0 -> break

                            numberOfCorrectWord -> {
                                correctWord.correctAnswersCount++

                                println("Правильно")
                                saveDictionary(dictionary)
                            }

                            else -> println("Не правильно")
                        }
                    }
                }
            }

            2 -> {
                val learnedWords =
                    dictionary.filter { word: Word -> word.correctAnswersCount >= REQUIRED_CORRECT_ANSWERS }
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

fun saveDictionary(dictionary: MutableList<Word>) {
    val file = File("words.txt")

    file.writeText("")
    for (word in dictionary) {
        file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
    }
}