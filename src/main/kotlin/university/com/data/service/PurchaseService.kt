package university.com.data.service

import university.com.data.model.Book
import university.com.data.model.Purchase
import java.math.BigInteger
import java.security.SecureRandom

class PurchaseService {
    private val purchases: MutableMap<String, MutableList<Purchase>> = mutableMapOf()
    private val random = SecureRandom()

    fun getPurchases(userId: String): List<Purchase> {
        return purchases.getOrElse(userId) { emptyList() }
    }

    fun addPurchase(userId: String, books: List<Book>) {
        val purchaseId = generateId()
        val purchase = purchases[userId]
        if (purchase != null) {
            purchase.add(Purchase(purchaseId, books))
        } else {
            purchases[userId] = mutableListOf(Purchase(purchaseId, books))
        }
    }

    private fun generateId(): String {
        return BigInteger(32 * 5, random).toString(32)
    }
}
