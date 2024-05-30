import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private const val HTTPS_API_TELEGRAM_ORG_BOT = "https://api.telegram.org/bot"

class TelegramBotService (private val botToken: String){
    fun getUpdates( updateId: Int): String {
        val urlGetUpdates = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage( chatId: Int, text: String): String {
        val urlSendMessage = "$HTTPS_API_TELEGRAM_ORG_BOT$botToken/sendMessage?chat_id=$chatId&text=$text"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}