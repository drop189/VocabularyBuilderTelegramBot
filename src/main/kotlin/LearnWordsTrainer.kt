import java.io.File

data class Statistics(
    val learnedWords: List<Word>,
    val percentageOfLearnedWords: Int
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {
    private var question: Question? = null

    val dictionary = loadDictionary()

    fun getStatistics(): Statistics {

        val learnedWords =
            dictionary.filter { word: Word -> word.correctAnswersCount >= REQUIRED_CORRECT_ANSWERS }
        val percentageOfLearnedWords = ((learnedWords.size.toDouble() / dictionary.size) * 100).toInt()

        return Statistics(learnedWords, percentageOfLearnedWords)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords =
            dictionary.filter { it.correctAnswersCount < REQUIRED_CORRECT_ANSWERS }

        if (unlearnedWords.isEmpty()) return null

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

        question = Question(answerOptions, correctWord)
        return question
    }

    fun checkAnswer(userAnswerInput: Int?): Boolean {
        return question?.let {
            val numberOfCorrectWord = it.variants.indexOf(it.correctAnswer)
            if (userAnswerInput == numberOfCorrectWord) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else false
        } ?: false
    }


    private fun loadDictionary(): MutableList<Word> {
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
        return dictionary
    }

    private fun saveDictionary(dictionary: MutableList<Word>) {
        val file = File("words.txt")

        file.writeText("")
        for (word in dictionary) {
            file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }
}