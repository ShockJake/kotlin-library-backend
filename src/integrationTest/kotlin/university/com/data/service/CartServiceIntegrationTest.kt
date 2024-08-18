package university.com.data.service

import org.junit.jupiter.api.TestInstance
import university.com.data.model.Book
import university.com.data.service.DataSupplier.getBook
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartServiceIntegrationTest {
    private val userId = "USER_ID"

    private lateinit var cartService: CartService
    private lateinit var purchaseService: PurchaseService

    @BeforeTest
    fun setUp() {
        purchaseService = PurchaseService()
        cartService = CartService(purchaseService)
        cartService.createCart(userId)
    }

    @Test
    fun shouldGetEmptyCart() {
        // given
        val expectedContents = listOf<Book>()

        // when
        val actualContents = cartService.getContents(userId)

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldAddBookToCart() {
        // given
        val testBook = getBook()
        val expectedContents = listOf(testBook)

        // when
        cartService.addBook(userId, testBook)
        val actualContents = cartService.getContents(userId)

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldRemoveBookFromCart() {
        // given
        val testBook = getBook()
        val expectedContents = listOf<Book>()

        // when
        cartService.addBook(userId, testBook)
        cartService.removeBook(userId, testBook)
        val actualContents = cartService.getContents(userId)

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldThrowExceptionWhenRemovingNonExistentBookFromCart() {
        // given
        val testBook = getBook()

        // when & then
        assertFailsWith<Exception> {
            cartService.removeBook(userId, testBook)
        }
    }

    @Test
    fun shouldClearCart() {
        // given
        val testBook = getBook()
        val expectedContents = listOf<Book>()

        // when
        cartService.addBook(userId, testBook)
        cartService.addBook(userId, testBook)
        cartService.cleanCart(userId)
        val actualContents = cartService.getContents(userId)

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldCheckoutCart() {
        // given
        val testBook = getBook()
        val expectedContents = listOf<Book>()
        val purchaseContents = listOf(testBook)

        // when
        cartService.addBook(userId, testBook)
        cartService.checkout(userId)
        val actualContents = cartService.getContents(userId)

        // then
        assertEquals(expectedContents, actualContents)
        assertEquals(purchaseContents, purchaseService.getPurchases(userId).first().books)
    }

    @Test
    fun shouldThrowExceptionWhenEmptyCartCheckout() {
        // when & then
        assertFailsWith<Exception> {
            cartService.checkout(userId)
        }
    }
}
