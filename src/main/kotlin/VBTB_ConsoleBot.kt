const val INDEX_CORRECTION = 1

private fun printQuestion(question: Question) {
    println("\t${question.correctAnswer.original}")
    question.variants.forEachIndexed { index, word ->
        println("${index + INDEX_CORRECTION}. ${word.translate}")
    }
    println("0. Главное меню")
}

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

                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(INDEX_CORRECTION))) {
                            println("Правильно")
                        } else println("Не правильно")
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