package university.com.data.service

import university.com.data.model.Book
import university.com.data.model.Purchase
import java.math.BigInteger
import java.security.SecureRandom

class PurchaseService {
    private val purchases: MutableList<Purchase> = mutableListOf()
    private val random = SecureRandom()

    fun getPurchases(): List<Purchase> {
        return purchases
    }

    fun addPurchase(books: List<Book>) {
        val id = generateId()
        purchases.add(Purchase(id, books))
    }

    private fun generateId(): String {
        return BigInteger(32 * 5, random).toString(32)
    }
}
