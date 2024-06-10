import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val HTTPS_API_TELEGRAM_ORG_BOT = "https://api.telegram.org/bot"

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)

class TelegramBotService(
    private val botToken: String,
    private val client: HttpClient = HttpClient.newBuilder().build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun getUpdates(updateId: Long): Response {
        val urlGetUpdates = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

        val responseString: String = try {
            client.send(request, HttpResponse.BodyHandlers.ofString()).body()
        } catch (e: Exception) {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy")
            val formattedDateTime = currentDateTime.format(formatter)
            println(formattedDateTime)
            println(e.message)
            "{\"ok\":false,\"result\":[]}"
        }

        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        return response
    }

    fun sendMessage(chatId: Long, text: String): String {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )
        return getResponseBody(requestBody)
    }

    fun sendMenu(chatId: Long): String {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучить слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_PROGRESS_CLICKED)
                    )
                )
            )
        )
        return getResponseBody(requestBody)
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Long) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendCompletionMessage(chatId)
        } else {
            sendQuestion(
                chatId,
                question
            )
        }
    }

    private fun sendQuestion(chatId: Long, question: Question): String {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(question.variants.mapIndexed { index, word ->
                listOf(
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                )
            } + listOf(listOf(InlineKeyboard(text = "Главное меню", callbackData = MAIN_MENU_CLICKED))))
        )
        return getResponseBody(requestBody)
    }

    private fun sendCompletionMessage(chatId: Long): String {
        val requestBody = SendMessageRequest(
            chatId,
            "Вы выучили все слова в базе",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(
                            text = "Главное меню",
                            callbackData = MAIN_MENU_CLICKED
                        )
                    )
                )
            )
        )
        return getResponseBody(requestBody)
    }

    private fun getResponseBody(requestBody: SendMessageRequest): String {
        val urlSendMessage = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/sendMessage"
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}