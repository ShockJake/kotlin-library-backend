package university.com.data.service

import io.ktor.util.logging.*
import university.com.data.model.Book
import university.com.data.model.Cart

class CartService(private val purchaseService: PurchaseService) {
    private val logger = KtorSimpleLogger(CartService::class.java.simpleName)
    private val cart = Cart(mutableListOf())

    fun addBook(book: Book) {
        logger.info("Adding book '${book.title}' to cart")
        cart.books.add(book)
    }

    fun removeBook(bookToRemove: Book) {
        logger.info("Removing book '${bookToRemove.title}' from cart")
        if (!cart.books.remove(bookToRemove)) {
            throw Exception("Cannot remove book '${bookToRemove.title}' from cart")
        }
    }

    fun checkout() {
        logger.info("Checkout")
        if (cart.books.isEmpty()) {
            throw Exception("No books in cart")
        }
        purchaseService.addPurchase(ArrayList(cart.books))
        cart.books.clear()
    }

    fun getContents(): List<Book> {
        return cart.books
    }

    fun cleanCart() {
        logger.info("Clearing cart")
        cart.books.clear()
    }
}
