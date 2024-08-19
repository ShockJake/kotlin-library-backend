package university.com.discordIntegration

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono
import discord4j.core.spec.InteractionCallbackSpecDeferReplyMono
import discord4j.core.spec.InteractionReplyEditMono
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import university.com.data.service.BookService
import university.com.data.service.DataSupplier.getCategories
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OperationExecutorTest {
    private val operationNames = listOf("hello", "categories", "get_books_by_category")
    private lateinit var bookService: BookService
    private lateinit var operationExecutor: OperationExecutor

    @BeforeTest
    fun setUp() {
        bookService = mock()
        whenever(bookService.getCategories()).thenReturn(getCategories())
        operationExecutor = OperationExecutor(operationNames, bookService)
    }

    @Test
    fun shouldSayHello() {
        // given
        val event: ChatInputInteractionEvent = mock()
        val mono: InteractionApplicationCommandCallbackReplyMono = mock()
        val expectedMessage = "Hi! I am a Librarian bot.\nHere are the commands:\n${
        operationNames.asSequence().map { "- `/$it`" }.joinToString("\n")
        }"
        whenever(event.reply(anyString())).thenReturn(mono)

        // when
        operationExecutor.sayHello.invoke(event)

        // then
        verify(event).reply(eq(expectedMessage))
    }

    @Test
    fun shouldGetCategories() {
        // given
        val categories = getCategories()
        val event: ChatInputInteractionEvent = mock()
        val mono: InteractionApplicationCommandCallbackReplyMono = mock()
        val expectedMessage = categories.asSequence().map { "- $it" }.joinToString("\n")
        whenever(event.reply(anyString())).thenReturn(mono)

        // when
        operationExecutor.getCategories.invoke(event)

        // then
        verify(event).reply(eq(expectedMessage))
    }

    @Test
    fun shouldGetBooksByCategory(): Unit = runBlocking {
        // given
        val category = getCategories().first()
        val bookServiceResult = listOf("TEST")
        val event: ChatInputInteractionEvent = mock()
        val editMono: InteractionReplyEditMono = mock()
        val deferMono: InteractionCallbackSpecDeferReplyMono = mock()
        val interactionOption: ApplicationCommandInteractionOption = mock()
        val optionValue: ApplicationCommandInteractionOptionValue = mock()
        whenever(event.deferReply()).thenReturn(deferMono)
        whenever(event.editReply(anyString())).thenReturn(editMono)
        whenever(event.options).thenReturn(listOf(interactionOption))
        whenever(interactionOption.value).thenReturn(Optional.of(optionValue))
        whenever(optionValue.asString()).thenReturn(category)
        whenever(bookService.fetchBooksDataAsText(category.lowercase(Locale.getDefault()))).thenReturn(bookServiceResult)

        val expectedMessage = "Here is what I found for category: $category\n\n${bookServiceResult.joinToString("\n")}"

        // when
        operationExecutor.getBooksByCategory.invoke(event)

        // then
        verify(event).editReply(eq(expectedMessage))
    }

    @Test
    fun shouldReplyIfNoCategoryFound(): Unit = runBlocking {
        // given
        val category = "TEST_CATEGORY"
        val event: ChatInputInteractionEvent = mock()
        val editMono: InteractionReplyEditMono = mock()
        val deferMono: InteractionCallbackSpecDeferReplyMono = mock()
        val interactionOption: ApplicationCommandInteractionOption = mock()
        val optionValue: ApplicationCommandInteractionOptionValue = mock()
        whenever(event.deferReply()).thenReturn(deferMono)
        whenever(event.editReply(anyString())).thenReturn(editMono)
        whenever(event.options).thenReturn(listOf(interactionOption))
        whenever(interactionOption.value).thenReturn(Optional.of(optionValue))
        whenever(optionValue.asString()).thenReturn(category)

        val expectedMessage = "Sorry, I can't find books for this category: $category"

        // when
        operationExecutor.getBooksByCategory.invoke(event)

        // then
        verify(event).editReply(eq(expectedMessage))
    }

    @Test
    fun shouldReplyIfExceptionWasThrown(): Unit = runBlocking {
        // given
        val category = getCategories().first()
        val event: ChatInputInteractionEvent = mock()
        val editMono: InteractionReplyEditMono = mock()
        val deferMono: InteractionCallbackSpecDeferReplyMono = mock()
        val interactionOption: ApplicationCommandInteractionOption = mock()
        val optionValue: ApplicationCommandInteractionOptionValue = mock()
        whenever(event.deferReply()).thenReturn(deferMono)
        whenever(event.editReply(anyString())).thenReturn(editMono)
        whenever(event.options).thenReturn(listOf(interactionOption))
        whenever(interactionOption.value).thenReturn(Optional.of(optionValue))
        whenever(optionValue.asString()).thenReturn(category)
        whenever(bookService.fetchBooksDataAsText(category.lowercase(Locale.getDefault()))).thenThrow(RuntimeException::class.java)

        val expectedMessage = "Sorry, for some reason I can't fetch data from my resources, try again later."

        // when
        operationExecutor.getBooksByCategory.invoke(event)

        // then
        verify(event).editReply(eq(expectedMessage))
    }
}
