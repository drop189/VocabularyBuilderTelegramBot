import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val HTTPS_API_TELEGRAM_ORG_BOT = "https://api.telegram.org/bot"

class TelegramBotService(private val botToken: String) {
    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Int, text: String): String {

        val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8)
        println(encoded)

        val urlSendMessage = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/sendMessage?chat_id=$chatId&text=$encoded"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMenu(chatId: Int): String {
        val urlSendMessage = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/sendMessage"

        val sendMenuBody = """
            {
            	"chat_id": $chatId,
            	"text": "Основное меню",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            					"text":"Изучить слова",
            					"callback_data": "$LEARN_WORDS_CLICKED"
            				},
            				{
            					"text":"Статистика",
            					"callback_data": "$STATISTICS_CLICKED"
            				}
            			]
            		]
            	}
            }
        """.trimIndent()


        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }


    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Int) {

        val question = trainer.getNextQuestion()

        if (question == null) {
            sendMessage(chatId, "Вы выучили все слова в базе")
        } else {
            sendQuestion(
                chatId,
                question
            )
        }
    }

    private fun sendQuestion(chatId: Int, question: Question?): String? {

        val urlSendMessage = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/sendMessage"

        val sendMenuBody = """
            {
	"chat_id": $chatId,
	"text": "${question?.correctAnswer?.original}",
	"reply_markup": {
		"inline_keyboard": [
			[
				{
					"text":"${question?.variants?.get(0)?.translate}",
					"callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 0}"
				},
				{
					"text":"${question?.variants?.get(1)?.translate}",
					"callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 1}"
				}
            ],
            [
                {
					"text":"${question?.variants?.get(2)?.translate}",
					"callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 2}"
				},
				{
					"text":"${question?.variants?.get(3)?.translate}",
					"callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 3}"
				}
            ]
		]
	}
}
        """.trimIndent()


        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}