package university.com.discord_integration

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import io.ktor.util.logging.*
import kotlinx.coroutines.runBlocking
import university.com.data.service.BookService
import java.util.*

class OperationExecutor(operationNames: List<String>, bookService: BookService) {
    private val logger: Logger = KtorSimpleLogger(OperationExecutor::class.java.simpleName)
    private val categories = bookService.getCategories()
    private val helloString = "Hi! I am a Librarian bot."
    private val categoriesMessage = categories.asSequence().map { "- $it" }.joinToString("\n")
    private val commandsMessage =
        "Here are the commands:\n${operationNames.asSequence().map { "- `/$it`" }.joinToString("\n")}"

    val sayHello: (ChatInputInteractionEvent) -> Unit = {
        it.reply("$helloString\n$commandsMessage").block()
    }

    val getCategories: (ChatInputInteractionEvent) -> Unit = {
        it.reply(categoriesMessage).block()
    }

    val getBooksByCategory: (ChatInputInteractionEvent) -> Unit = {
        it.deferReply().block()
        val categoryName = it.options[0].value.get().asString()
        if (categories.contains(categoryName)) {
            try {
                val result =
                    runBlocking { bookService.fetchBooksDataAsText(categoryName.lowercase(Locale.getDefault())) }
                logger.info("Sending data...")
                it.editReply("Here is what I found for category: $categoryName\n\n${result.joinToString("\n")}").block()
            } catch (e: Exception) {
                logger.error(e.message)
                it.editReply("Sorry, for some reason I can't fetch data from my resources, try again later.").block()
            }
        } else {
            logger.warn("Requested unknown category: $categoryName")
            it.editReply("Sorry, I can't find books for this category: $categoryName").block()
        }
    }
}
