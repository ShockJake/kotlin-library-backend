package university.com.data.service

import org.junit.jupiter.api.TestInstance
import university.com.data.model.Purchase
import university.com.data.service.DataSupplier.getBook
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PurchaseServiceTest {
    private val userId = "USER_ID"

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
        val actual = service.getPurchases(userId)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun shouldAddAndGetOnePurchase() {
        // given
        val testBooks = mutableListOf(getBook())

        // when
        service.addPurchase(userId, testBooks)
        val purchases = service.getPurchases(userId)

        // then
        assertEquals(testBooks, purchases.first().books)
    }

    @Test
    fun shouldAddAndGetThreePurchases() {
        // given
        val expectedSize = 3
        val testBooks = mutableListOf(getBook())

        // when
        service.addPurchase(userId, testBooks)
        service.addPurchase(userId, testBooks)
        service.addPurchase(userId, testBooks)
        val purchases = service.getPurchases(userId)

        // then
        assertEquals(expectedSize, purchases.size)
        assertNotEquals(purchases.first().id, purchases.last().id)
        assertNotEquals(purchases[1].id, purchases.last().id)
    }
}
