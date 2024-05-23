import java.io.File

const val REQUIRED_CORRECT_ANSWERS = 3
const val NUMBER_OF_ANSWERS = 4

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
                            var setOfWords = unlearnedWords.toSet()

                            while (setOfWords.size < NUMBER_OF_ANSWERS){
                                setOfWords = (unlearnedWords + dictionary).toSet()
                            }

                            answerOptions = setOfWords.shuffled().take(NUMBER_OF_ANSWERS)
                            correctWord = answerOptions.random()
                        } else {

                            answerOptions = unlearnedWords.shuffled().take(NUMBER_OF_ANSWERS)
                            correctWord = answerOptions.random()
                        }

                        println("\t${correctWord.original}")
                        answerOptions.forEachIndexed { index, word -> println("${index + 1}. ${word.translate}") }

                        break
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