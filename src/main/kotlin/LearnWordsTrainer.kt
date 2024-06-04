import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

data class Statistics(
    val learnedWords: List<Word>,
    val percentageOfLearnedWords: Int
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val numberOfAnswers: Int = 4,
    private val requiredCorrectAnswers: Int = 3,
) {
    var question: Question? = null

    val dictionary = loadDictionary()

    fun getStatistics(): Statistics {

        val learnedWords =
            dictionary.filter { word: Word -> word.correctAnswersCount >= requiredCorrectAnswers }
        val percentageOfLearnedWords = ((learnedWords.size.toDouble() / dictionary.size) * 100).toInt()

        return Statistics(learnedWords, percentageOfLearnedWords)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords =
            dictionary.filter { it.correctAnswersCount < requiredCorrectAnswers }
        if (unlearnedWords.isEmpty()) return null

        val answerOptions: List<Word>
        val correctWord: Word

        if (unlearnedWords.size < numberOfAnswers) {

            val learnedWords =
                dictionary.filter { it.correctAnswersCount >= requiredCorrectAnswers }

            val listOfWords =
                unlearnedWords + learnedWords.take(numberOfAnswers - unlearnedWords.size)

            answerOptions = listOfWords.shuffled().take(numberOfAnswers)
            correctWord = unlearnedWords.random()
        } else {

            answerOptions = unlearnedWords.shuffled().take(numberOfAnswers)
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
                saveDictionary()
                true
            } else false
        } ?: false
    }


    private fun loadDictionary(): List<Word> {
        val wordsFile = File(fileName)
        if (!wordsFile.exists()) {
            File("words.txt").copyTo(wordsFile)
        }
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

    private fun saveDictionary() {
        val file = File(fileName)
        file.writeText("")
        for (word in dictionary) {
            file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }
}