private const val DELAY_MS = 2000L

const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0

    val trainer = LearnWordsTrainer()

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":(.+?),\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(DELAY_MS)

        val updates = telegramBotService.getUpdates(updateId)
        println(updates) //

        val updateIdInt = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        updateId = updateIdInt + 1

        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        println(text)
        println(chatId)


        if (text.equals("Hello", ignoreCase = true) && chatId != null) telegramBotService.sendMessage(
            chatId,
            "Hello"
        )
        if (text.equals("Menu", ignoreCase = true) && chatId != null) telegramBotService.sendMenu(chatId)
        /***/
        if (text.equals("/start", ignoreCase = true) && chatId != null) telegramBotService.sendMenu(chatId)
        if (data.equals(LEARN_WORDS_CLICKED, ignoreCase = true) && chatId != null) {
            telegramBotService.checkNextQuestionAndSend(trainer, chatId)
        }
        if (data.equals(STATISTICS_CLICKED, ignoreCase = true) && chatId != null) {

            val statistics = trainer.getStatistics()

            telegramBotService.sendMessage(
                chatId,
                "Выучено ${statistics.learnedWords.size} из " +
                        "${trainer.dictionary.size} слов | " +
                        "${statistics.percentageOfLearnedWords}%"
            )
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) ?: continue && chatId != null) {
            val indexOfAnswer = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            val isCorrect = trainer.checkAnswer(indexOfAnswer)

            if (isCorrect) {
                telegramBotService.sendMessage(chatId, "Правильно")
            } else {
                telegramBotService.sendMessage(
                    chatId,
                    "Не правильно: " +
                            "${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
                )
            }

            telegramBotService.checkNextQuestionAndSend(trainer, chatId)
        }
    }
}