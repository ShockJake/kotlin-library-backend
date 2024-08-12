package university.com.data.service

import university.com.data.model.Book
import university.com.data.service.DataSupplier.getBook
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CartServiceTest {
    private lateinit var cartService: CartService
    private lateinit var purchaseService: PurchaseService

    @BeforeTest
    fun setUp() {
        purchaseService = PurchaseService()
        cartService = CartService(purchaseService)
    }

    @Test
    fun shouldGetEmptyCart() {
        // given
        val expectedContents = listOf<Book>()

        // when
        val actualContents = cartService.getContents()

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldAddBookToCart() {
        // given
        val testBook = getBook()
        val expectedContents = listOf(testBook)

        // when
        cartService.addBook(testBook)
        val actualContents = cartService.getContents()

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldRemoveBookFromCart() {
        // given
        val testBook = getBook()
        val expectedContents = listOf<Book>()

        // when
        cartService.addBook(testBook)
        cartService.removeBook(testBook)
        val actualContents = cartService.getContents()

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldThrowExceptionWhenRemovingNonExistentBookFromCart() {
        // given
        val testBook = getBook()

        // when & then
        assertFailsWith<Exception> {
            cartService.removeBook(testBook)
        }
    }

    @Test
    fun shouldClearCart() {
        // given
        val testBook = getBook()
        val expectedContents = listOf<Book>()

        // when
        cartService.addBook(testBook)
        cartService.addBook(testBook)
        cartService.cleanCart()
        val actualContents = cartService.getContents()

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
        cartService.addBook(testBook)
        cartService.checkout()
        val actualContents = cartService.getContents()

        // then
        assertEquals(expectedContents, actualContents)
        assertEquals(purchaseContents, purchaseService.getPurchases().first().books)
    }

    @Test
    fun shouldThrowExceptionWhenEmptyCartCheckout() {
        // when & then
        assertFailsWith<Exception> {
            cartService.checkout()
        }
    }
}
