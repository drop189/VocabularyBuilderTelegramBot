import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val HTTPS_API_TELEGRAM_ORG_BOT = "https://api.telegram.org/bot"
const val SECONDS_IN_48_HOURS = 172800

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
data class EditMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("message_id")
    val messageId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class DeleteMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("message_id")
    val messageId: Long?,
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

data class State(
    val messageId: Long,
    val unixTime: Long,
)

class TelegramBotService(
    private val botToken: String,
    private val client: HttpClient = HttpClient.newBuilder().build(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun getUpdates(updateId: Long): Response {
        val urlGetUpdates = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

        val responseString: String = try {
            client.send(request, HttpResponse.BodyHandlers.ofString()).body()
        } catch (exception: Exception) {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy")
            val formattedDateTime = currentDateTime.format(formatter)
            println(formattedDateTime)
            println(exception.message)
            "{\"ok\":false,\"result\":[]}"
        }

        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        return response
    }

    fun sendMessage(chatId: Long, text: String): String? {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )
        return getResponseBody(requestBody)
    }

    fun sendMenu(chatId: Long): String? {
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
            return
        }

        val stateFile = File("${chatId}_state.txt")
        if (!stateFile.exists()) {
            sendQuestion(chatId, question)
            return
        }
        val stateLine = stateFile.readLines().firstOrNull() ?: ""
        val stateParts = stateLine.split("|")
        if (stateParts.size != 2) {
            sendQuestion(chatId, question)
            return
        }

        val state = try {
            State(
                messageId = stateParts[0].toLong(),
                unixTime = stateParts[1].toLong()
            )
        } catch (e: Exception) {
            sendQuestion(chatId, question)
            return
        }

        val currentUnixTime = System.currentTimeMillis() / 1000 // В секундах
        val lastMessageId = tempStorageOfMessageId[chatId]

        if (lastMessageId == null || (currentUnixTime - state.unixTime) >= SECONDS_IN_48_HOURS) {
            sendQuestion(chatId, question)
        } else {
            sendQuestion(chatId, lastMessageId, question)
        }
    }


    fun sendWrongAnswer(chatId: Long, trainer: LearnWordsTrainer): String? {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Не правильно: " +
                    "${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(
                            text = "Далее", callbackData = CALLBACK_DATA_NEXT
                        )
                    )
                )
            )
        )
        return getResponseBody(requestBody)
    }

    fun editMessage(chatId: Long, messageId: Long, message: String): String? {
        val requestBody = EditMessageRequest(
            chatId = chatId,
            messageId = messageId,
            text = message,
        )
        return getResponseBody(requestBody)
    }

    fun deleteMessage(chatId: Long, messageId: Long): String? {
        val requestBody = DeleteMessageRequest(
            chatId = chatId,
            messageId = messageId,
        )
        return getResponseBody(requestBody)
    }

    private fun sendQuestion(chatId: Long, question: Question): String? {
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

    private fun sendQuestion(chatId: Long, messageId: Long?, question: Question): String? {
        val requestBody = EditMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            messageId = messageId,
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

    private fun sendCompletionMessage(chatId: Long): String? {
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

    private fun getResponseBody(requestBody: SendMessageRequest): String? {
        val urlSendMessage = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/sendMessage"
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }

        val responseString = responseResult.getOrNull()?.body()
        val response = json.decodeFromString<SendResponse>(responseString ?: "")
        val chatId = response.message?.chat?.id
        val messageId = response.message?.messageId
        val messageDate = response.message?.date
//Сохранение
        val file = File("${chatId}_state.txt")
        file.writeText("")
        file.appendText("${messageId}|${messageDate}")
        tempStorageOfMessageId[chatId] = response.message?.messageId

        return responseString
    }

    private fun getResponseBody(requestBody: EditMessageRequest): String? {
        val urlSendMessage = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/editMessageText"
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return responseResult.getOrNull()?.body()
    }

    private fun getResponseBody(requestBody: DeleteMessageRequest): String? {
        val urlSendMessage = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/deleteMessage"
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return responseResult.getOrNull()?.body()
    }
}