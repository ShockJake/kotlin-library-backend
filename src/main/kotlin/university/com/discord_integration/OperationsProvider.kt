package university.com.discord_integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.discordjson.json.ImmutableApplicationCommandRequest
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import university.com.data.service.BookService

class OperationsProvider {
    private val builder = ApplicationCommandRequest.builder()
    private val executor = OperationExecutor(
        listOf("hello", "categories", "get_books_by_category"),
        BookService(
            HttpClient(CIO) { install(HttpTimeout) { requestTimeoutMillis = 30000 } },
            jacksonObjectMapper()
        )
    )


    private val hello: ImmutableApplicationCommandRequest = builder
        .name("hello")
        .description("Provides initial info")
        .build()

    private val categories: ImmutableApplicationCommandRequest = builder
        .name("categories")
        .description("Returns a list of categories")
        .build()

    private val getBooksByCategory: ImmutableApplicationCommandRequest = builder
        .name("get_books_by_category")
        .description("Returns a list of books by given category")
        .addOption(
            ApplicationCommandOptionData.builder()
                .name("category_name")
                .description("Name of category for the books you want to get")
                .type(ApplicationCommandOption.Type.STRING.value)
                .required(true).build()
        ).build()


    val operations: Map<String, Pair<ImmutableApplicationCommandRequest, (ChatInputInteractionEvent) -> Unit>> =
        mapOf(
            "hello" to Pair(hello, executor.sayHello),
            "categories" to Pair(categories, executor.getCategories),
            "get_books_by_category" to Pair(getBooksByCategory, executor.getBooksByCategory)
        )
}
