import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val DELAY_MS = 2000L

const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val RESET_PROGRESS_CLICKED = "reset_progress_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val MAIN_MENU_CLICKED = "main_menu_clicked"

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String? = null,
    @SerialName("chat")
    val chat: Chat,
    @SerialName("from")
    val from: From,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class From(
    @SerialName("language_code")
    val languageCode: String,
    @SerialName("first_name")
    val firstName: String,
)


fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(DELAY_MS)
        val response: Response = telegramBotService.getUpdates(lastUpdateId)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, telegramBotService, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    telegramBotService: TelegramBotService,
    trainers: HashMap<Long, LearnWordsTrainer>
) {
    val text = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data
    val languageCode = update.message?.from?.languageCode
    val firstName = update.message?.from?.firstName
    val trainer = trainers.getOrPut(chatId) {
        LearnWordsTrainer("$chatId.txt")
    }

    println(text)
    println(chatId)

    if (text.equals("Hello", ignoreCase = true) or
        text.equals("Привет", ignoreCase = true) or
        text.equals("\uD83D\uDC4B", ignoreCase = true)
    ) {
        if (languageCode == "ru") telegramBotService.sendMessage(
            chatId,
            "Привет, $firstName \uD83D\uDC4B"
        )
        else telegramBotService.sendMessage(
            chatId,
            "Hello, $firstName \uD83D\uDC4B"
        )
    }

    if (text.equals("Menu", ignoreCase = true) or
        text.equals("/start", ignoreCase = true) or
        data.equals(MAIN_MENU_CLICKED, ignoreCase = true)
    ) {
        telegramBotService.sendMenu(chatId)
    }
    if (data.equals(LEARN_WORDS_CLICKED, ignoreCase = true)) {
        telegramBotService.checkNextQuestionAndSend(trainer, chatId)
    }
    if (data.equals(STATISTICS_CLICKED, ignoreCase = true)) {
        val statistics = trainer.getStatistics()

        telegramBotService.sendMessage(
            chatId,
            "Выучено ${statistics.learnedWords.size} из " +
                    "${trainer.dictionary.size} слов | " +
                    "${statistics.percentageOfLearnedWords}%"
        )
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) ?: return) {
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
    if (data.equals(RESET_PROGRESS_CLICKED, ignoreCase = true)) {
        trainer.resetProgress()
        telegramBotService.sendMessage(chatId, "Прогресс сброшен")
    }
}
