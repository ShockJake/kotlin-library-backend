package university.com.common

import university.com.data.service.CartService
import university.com.data.service.PurchaseService
import university.com.security.UserService

object ServiceProvider {
    private val purchaseService = PurchaseService()
    private val cartService = CartService(purchaseService)
    private val userService = UserService(cartService)

    fun getPurchaseService() = purchaseService
    fun getCartService() = cartService
    fun getUserService() = userService
}
