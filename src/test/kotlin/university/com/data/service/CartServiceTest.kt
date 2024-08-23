package university.com.data.service

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import university.com.data.model.Book
import university.com.data.service.DataSupplier.getBook
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartServiceTest {
    private val userId = "USER_ID"

    private lateinit var cartService: CartService
    private lateinit var purchaseService: PurchaseService

    @BeforeTest
    fun setUp() {
        purchaseService = mock()
        cartService = CartService(purchaseService)
    }

    @Test
    fun shouldCreateCartForUserId() {
        // when & then
        assertDoesNotThrow {
            cartService.createCart(userId)
        }
    }

    @Test
    fun shouldThrowExceptionIfCartIsAlreadyExist() {
        // given
        cartService.createCart(userId)

        // when & then
        assertThrows<IllegalStateException> {
            cartService.createCart(userId)
        }
    }

    @Test
    fun shouldGetEmptyCart() {
        // given
        cartService.createCart(userId)
        val expectedContents = listOf<Book>()

        // when
        val actualContents = cartService.getContents(userId)

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldThrowExceptionIfGettingCartThatDoesNotExist() {
        // when & then
        assertThrows<IllegalStateException> {
            cartService.getContents(userId)
        }
    }

    @Test
    fun shouldAddBookToCart() {
        // given
        cartService.createCart(userId)
        val testBook = getBook()
        val expectedContents = listOf(testBook)

        // when
        cartService.addBook(userId, testBook)
        val actualContents = cartService.getContents(userId)

        // then
        assertEquals(expectedContents, actualContents)
    }

    @Test
    fun shouldThrowExceptionIfAddingBookToCartThatDoesNotExist() {
        // when & then
        assertThrows<IllegalStateException> {
            cartService.addBook(userId, getBook())
        }
    }

    @Test
    fun shouldRemoveBookFromCart() {
        // given
        cartService.createCart(userId)
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
    fun shouldThrowExceptionIfRemovingBookFromCartThatDoesNotExist() {
        // when & then
        assertThrows<IllegalStateException> {
            cartService.removeBook(userId, getBook())
        }
    }

    @Test
    fun shouldThrowExceptionWhenRemovingNonExistentBookFromCart() {
        // given
        cartService.createCart(userId)
        val testBook = getBook()

        // when & then
        assertFailsWith<Exception> {
            cartService.removeBook(userId, testBook)
        }
    }

    @Test
    fun shouldClearCart() {
        // given
        cartService.createCart(userId)
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
        cartService.createCart(userId)
        val testBook = getBook()
        val expectedContents = listOf<Book>()
        val purchaseContents = listOf(testBook)

        // when
        cartService.addBook(userId, testBook)
        cartService.checkout(userId)
        val actualContents = cartService.getContents(userId)

        // then
        assertEquals(expectedContents, actualContents)
        verify(purchaseService).addPurchase(userId, purchaseContents)
    }

    @Test
    fun shouldThrowExceptionWhenEmptyCartCheckout() {
        cartService.createCart(userId)

        // when & then
        assertFailsWith<Exception> {
            cartService.checkout(userId)
        }
    }

    @Test
    fun shouldThrowExceptionWhenCheckoutCartThatDoesNotExist() {
        // when & then
        assertThrows<IllegalStateException> {
            cartService.checkout(userId)
        }
    }
}
