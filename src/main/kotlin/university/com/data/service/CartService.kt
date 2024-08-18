package university.com.data.service

import io.ktor.util.logging.KtorSimpleLogger
import university.com.data.model.Book
import university.com.data.model.Cart

class CartService(private val purchaseService: PurchaseService) {
    private val logger = KtorSimpleLogger(CartService::class.java.simpleName)
    private val carts = mutableMapOf<String, Cart>()

    fun addBook(userId: String, book: Book) {
        logger.info("Adding book '${book.title}' to cart or user $userId")
        val cart = carts.getOrElse(userId) { error("Cart with id $userId not found") }
        cart.books.add(book)
    }

    fun removeBook(userId: String, bookToRemove: Book) {
        logger.info("Removing book '${bookToRemove.title}' from cart of user $userId")
        val cart = carts.getOrElse(userId) { error("Cart with id $userId not found") }
        if (!cart.books.remove(bookToRemove)) {
            throw Exception("Cannot remove book '${bookToRemove.title}' from cart")
        }
    }

    fun checkout(userId: String) {
        logger.info("Checkout for user $userId")
        val books = carts.getOrElse(userId) { error("Cart with id $userId not found") }.books
        if (books.isEmpty()) {
            error("Cannot checkout empty cart")
        }
        purchaseService.addPurchase(userId, ArrayList(books))
        books.clear()
    }

    fun getContents(userId: String): List<Book> {
        return carts.getOrElse(userId) { error("Cart with user id $userId cannot be found") }.books
    }

    fun cleanCart(userId: String) {
        logger.info("Clearing cart for user $userId")
        val cart = carts.getOrElse(userId) { error("Cart with user $userId cannot be found") }
        cart.books.clear()
    }

    fun createCart(userId: String) {
        if (carts.containsKey(userId)) {
            error("Cart for $userId - already exists")
        }
        carts[userId] = Cart(mutableListOf())
    }
}
