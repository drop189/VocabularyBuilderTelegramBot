private const val DELAY_MS = 2000L

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0
    
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":(.+?),\"".toRegex()

    while (true) {
        Thread.sleep(DELAY_MS)

        val updates = telegramBotService.getUpdates(updateId)

        println(updates) //

        val startUpdateId = updates.lastIndexOf("update_id")
        val endUpdateId = updates.lastIndexOf(",\n\"message\"")

        if (startUpdateId == -1 || endUpdateId == -1) continue

        val updateIdString = updates.substring(startUpdateId + 11, endUpdateId)

        updateId = updateIdString.toInt() + 1


        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups: MatchGroupCollection? = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)

        val matchResultChatId: MatchResult? = chatIdRegex.find(updates)
        val groupsChatId: MatchGroupCollection? = matchResultChatId?.groups
        val chatId = groupsChatId?.get(1)?.value
        println(chatId)

        if (text.equals("Hello", ignoreCase = true)) telegramBotService.sendMessage(
            chatId?.toIntOrNull() ?: 0,
            "Hello"
        )
    }
}