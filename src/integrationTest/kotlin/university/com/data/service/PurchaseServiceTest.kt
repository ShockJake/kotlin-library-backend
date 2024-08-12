package university.com.data.service

import university.com.data.model.Purchase
import university.com.data.service.BooksAsserter.assertBookLists
import university.com.data.service.DataSupplier.getBook
import university.com.data.service.DataSupplier.getBooksAsObjects
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PurchaseServiceTest {
    private lateinit var service: PurchaseService

    @BeforeTest
    fun setUp() {
        service = PurchaseService()
    }

    @Test
    fun shouldGetZeroPurchases() {
        // given
        val expected = mutableListOf<Purchase>()

        // when
        val actual = service.getPurchases()

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun shouldAddAndGetOnePurchase() {
        // given
        val testBooks = getBooksAsObjects()

        // when
        service.addPurchase(testBooks)
        val purchases = service.getPurchases()

        // then
        val purchasedBooks = purchases.first().books
        assertBookLists(testBooks, purchasedBooks)
    }

    @Test
    fun shouldAddAndGetThreePurchases() {
        // given
        val expectedSize = 3
        val testBooks = mutableListOf(getBook())

        // when
        service.addPurchase(testBooks)
        service.addPurchase(testBooks)
        service.addPurchase(testBooks)
        val purchases = service.getPurchases()

        // then
        assertEquals(expectedSize, purchases.size)
        assertNotEquals(purchases.first().id, purchases.last().id)
        assertNotEquals(purchases[1].id, purchases.last().id)
    }
}
