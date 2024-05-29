fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService()
    var updateId = 0

    while (true) {
        Thread.sleep(2000)

        val updates = telegramBotService.getUpdates(botToken, updateId)
        println(updates) //

        val startUpdateId = updates.lastIndexOf("update_id")
        val endUpdateId = updates.lastIndexOf(",\n\"message\"")

        if (startUpdateId == -1 || endUpdateId == -1) continue

        val updateIdString = updates.substring(startUpdateId + 11, endUpdateId)

        updateId = updateIdString.toInt() + 1


        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups: MatchGroupCollection? = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)

        val chatIdRegex: Regex = "\"chat\":\\{\"id\":(.+?),\"".toRegex()
        val matchResultChatId: MatchResult? = chatIdRegex.find(updates)
        val groupsChatId: MatchGroupCollection? = matchResultChatId?.groups
        val chatId = groupsChatId?.get(1)?.value
        println(chatId)

        if (text.equals("Hello", ignoreCase = true)) telegramBotService.sendMessage(
            botToken,
            chatId?.toIntOrNull() ?: 0,
            "Hello"
        )
    }
}