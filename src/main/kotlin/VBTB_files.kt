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

    val trainer = LearnWordsTrainer()

    while (true) {
        println(
            "Меню: \n" +
                    "1 – Учить слова\n" +
                    "2 – Статистика \n" +
                    "0 – Выход"
        )

        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Поздравляю, вы выучили все слова")
                        return
                    } else {
                        printQuestion(question)
                        trainer.checkAnswer() ?: break
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()

                println(
                    "Выучено ${statistics.learnedWords.size} из " +
                            "${trainer.dictionary.size} слов | " +
                            "${statistics.percentageOfLearnedWords}%"
                )
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

private fun printQuestion(question: Question) {
    println("\t${question.correctAnswer.original}")
    question.variants.forEachIndexed { index, word ->
        println("${index + INDEX_CORRECTION}. ${word.translate}")
    }
    println("0. Главное меню")
}