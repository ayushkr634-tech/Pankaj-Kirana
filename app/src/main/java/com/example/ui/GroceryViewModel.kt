package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroceryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GroceryDatabase.getDatabase(application)
    private val repository = GroceryRepository(database.groceryDao())
    private val geminiService = GeminiService()

    // --- UI Navigation & Configuration State ---
    var currentScreen = mutableStateOf("SPLASH")
    var selectedProductId = mutableStateOf<Int?>(null)
    var activeOrderId = mutableStateOf<Int?>(null)
    
    // Theme & Language Settings
    var isDarkTheme = mutableStateOf(false)
    var currentLanguage = mutableStateOf("EN") // "EN" or "HI"

    // --- Flow-based State from Repository ---
    val allProducts: StateFlow<List<GroceryProduct>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<WishlistItem>> = repository.wishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAddresses: StateFlow<List<AddressEntity>> = repository.allAddresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Search & Filter State ---
    var searchQuery = mutableStateOf("")
    var selectedCategory = mutableStateOf<String?>(null)
    var filterBrand = mutableStateOf<String?>(null)
    var sortOption = mutableStateOf("Popular") // "Popular", "Low to High", "High to Low", "Best Rated"

    // --- AI Assistant Chat State ---
    private val _aiChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiChatHistory = _aiChatHistory.asStateFlow()
    
    var isAiLoading = mutableStateOf(false)

    // --- Authentication Flow State (Local simulation) ---
    var isLoggedIn = mutableStateOf(true) // Start logged in for seamless demo, but toggleable
    var mobileNumberInput = mutableStateOf("")
    var emailInput = mutableStateOf("")
    var otpInput = mutableStateOf("")
    var showOtpDialog = mutableStateOf(false)
    var currentAddressSelectionId = mutableStateOf<Int?>(null)

    // Coupon / Code State
    var appliedCouponCode = mutableStateOf<String?>(null)
    var checkoutDeliveryMethod = mutableStateOf("Home Delivery") // "Home Delivery", "Express Delivery", "Store Pickup"

    init {
        // Seed database if empty
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            // Set first address as default selected if exists
            val addresses = repository.allAddresses.firstOrNull() ?: emptyList()
            if (addresses.isNotEmpty()) {
                currentAddressSelectionId.value = addresses.first().id
            }
        }
    }

    // --- Translations Helper ---
    fun t(en: String, hi: String): String {
        return if (currentLanguage.value == "HI") hi else en
    }

    // --- Catalog Actions ---
    fun setProductCategory(category: String?) {
        selectedCategory.value = if ((category == "All") || (category == "सभी")) null else category
    }

    fun toggleWishlist(productId: Int) {
        viewModelScope.launch {
            val isFav = wishlistItems.value.any { it.productId == productId }
            if (isFav) {
                repository.deleteWishlistItem(productId)
            } else {
                repository.insertWishlistItem(WishlistItem(productId = productId))
            }
        }
    }

    // --- Cart Actions ---
    fun addToCart(productId: Int, qty: Int = 1) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == productId }
            if (existing != null) {
                repository.updateCartItem(existing.copy(quantity = existing.quantity + qty))
            } else {
                repository.insertCartItem(CartItem(productId = productId, quantity = qty))
            }
        }
    }

    fun decreaseCartQty(productId: Int) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == productId } ?: return@launch
            if (existing.quantity > 1) {
                repository.updateCartItem(existing.copy(quantity = existing.quantity - 1))
            } else {
                repository.deleteCartItem(existing)
            }
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == productId } ?: return@launch
            repository.deleteCartItem(existing)
        }
    }

    fun saveForLater(productId: Int, save: Boolean) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == productId } ?: return@launch
            repository.updateCartItem(existing.copy(isSavedForLater = save))
        }
    }

    // --- Order / Checkout Actions ---
    fun getCartSummary(productsList: List<GroceryProduct>, itemsList: List<CartItem>): CartSummary {
        var subtotal = 0.0
        var totalSavings = 0.0
        
        itemsList.filter { !it.isSavedForLater }.forEach { item ->
            val prod = productsList.find { it.id == item.productId }
            if (prod != null) {
                val baseCost = prod.price * item.quantity
                val saving = (prod.price * (prod.discount / 100.0)) * item.quantity
                subtotal += baseCost - saving
                totalSavings += saving
            }
        }

        // Apply coupon discount (Disabled)
        val discountAmount = 0.0

        val gst = 0.0 // GST Removed

        val handlingCharge = 10.0 // Flat 10 Rupees handling charge

        val total = subtotal - discountAmount + gst + handlingCharge

        return CartSummary(
            subtotal = subtotal,
            discountAmount = discountAmount,
            gst = gst,
            handlingCharge = handlingCharge,
            totalSavings = totalSavings,
            totalAmount = total,
        )
    }

    fun placeOrder(summary: CartSummary, addressText: String, instructions: String) {
        viewModelScope.launch {
            val productsList = allProducts.value
            val activeCartItems = cartItems.value.filter { !it.isSavedForLater }
            if (activeCartItems.isEmpty()) return@launch

            val orderItemsList = activeCartItems.mapNotNull { item ->
                val prod = productsList.find { it.id == item.productId }
                if (prod != null) {
                    val finalPrice = prod.price * (1 - (prod.discount / 100.0))
                    OrderProductInfo(
                        productId = prod.id,
                        productName = prod.name,
                        quantity = item.quantity,
                        priceAtOrder = finalPrice
                    )
                } else null
            }

            val itemsJson = repository.serializeOrderItems(orderItemsList)
            val estimatedDel = when (checkoutDeliveryMethod.value) {
                "Express Delivery" -> "Within 45 Mins"
                "Store Pickup" -> "Ready in 30 Mins"
                else -> "Within 2-3 Hours"
            }

            val newOrder = OrderEntity(
                timestamp = System.currentTimeMillis(),
                itemsJson = itemsJson,
                totalAmount = summary.totalAmount,
                deliveryAddress = addressText,
                deliveryInstructions = instructions,
                deliveryTime = checkoutDeliveryMethod.value,
                paymentMethod = "Cash on Delivery",
                status = "Order Confirmed",
                estimatedDelivery = estimatedDel
            )



            repository.insertOrder(newOrder)
            repository.clearCart()
            
            // Redirect to Order Tracking screen with the latest order ID
            val insertedOrders = repository.allOrders.first()
            val latestOrder = insertedOrders.maxByOrNull { it.timestamp }
            latestOrder?.let {
                activeOrderId.value = it.id
                currentScreen.value = "ORDER_TRACKING"
            }
        }
    }

    fun reorder(order: OrderEntity) {
        viewModelScope.launch {
            val items = repository.deserializeOrderItems(order.itemsJson)
            items.forEach { item ->
                addToCart(item.productId, item.quantity)
            }
            currentScreen.value = "CART"
        }
    }

    // --- Admin Dashboard Metrices ---
    fun getAdminStats(ordersList: List<OrderEntity>, productsList: List<GroceryProduct>): AdminStats {
        val totalOrders = ordersList.size
        val deliveredOrders = ordersList.filter { it.status == "Delivered" }
        val pendingOrders = ordersList.filter { it.status != "Delivered" }
        
        val totalRevenue = deliveredOrders.sumOf { it.totalAmount }
        
        // Count low stock alert products (stock < 10)
        val lowStockCount = productsList.count { it.stock < 10 }
        
        return AdminStats(
            totalOrders = totalOrders,
            deliveredOrdersCount = deliveredOrders.size,
            pendingOrdersCount = pendingOrders.size,
            totalRevenue = totalRevenue,
            lowStockCount = lowStockCount
        )
    }

    // --- Admin Control Actions ---
    fun adminUpdateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            val ordersList = allOrders.value
            val order = ordersList.find { it.id == orderId } ?: return@launch
            repository.updateOrder(order.copy(status = newStatus))
        }
    }

    fun adminAddProduct(
        name: String, brand: String, category: String, weight: String,
        price: Double, discount: Double, stock: Int, description: String
    ) {
        viewModelScope.launch {
            val newProd = GroceryProduct(
                name = name,
                brand = brand,
                category = category,
                weight = weight,
                price = price,
                discount = discount,
                stock = stock,
                description = description,
                ingredients = "Quality product from local farmers",
                nutritionalInfo = "Healthy and authentic packaging",
                expiryDate = "Best before 6 months from packaging",
                rating = 5.0,
                reviewCount = 1,
                isFeatured = true,
                isBestSeller = false,
                isNewArrival = true
            )
            repository.insertProduct(newProd)
        }
    }

    fun adminUpdateProductStock(productId: Int, newStock: Int) {
        viewModelScope.launch {
            val prod = allProducts.value.find { it.id == productId } ?: return@launch
            repository.updateProduct(prod.copy(stock = newStock))
        }
    }

    fun adminDeleteProduct(product: GroceryProduct) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // --- Address Actions ---
    fun addAddress(title: String, line: String) {
        viewModelScope.launch {
            repository.insertAddress(AddressEntity(title = title, addressLine = line))
        }
    }

    fun deleteAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.deleteAddress(address)
        }
    }

    // --- AI Assistant Messaging ---
    fun sendAiMessage(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            isAiLoading.value = true
            val currentHistory = _aiChatHistory.value
            
            _aiChatHistory.value = currentHistory + (message to "") // User's message
            
            val aiResponse = geminiService.askAssistant(message, currentHistory)
            
            // Replace the empty string placeholder with the real response
            _aiChatHistory.value = currentHistory + (message to aiResponse)
            isAiLoading.value = false
        }
    }

    fun clearAiChat() {
        _aiChatHistory.value = emptyList()
    }

    // --- Profile Update ---
    fun updateProfile(name: String, email: String, mobile: String) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                repository.saveUserProfile(
                    profile.copy(name = name, email = email, mobile = mobile)
                )
            }
        }
    }

    // --- Utility deserialization helper ---
    fun getOrderProducts(order: OrderEntity): List<OrderProductInfo> {
        return repository.deserializeOrderItems(order.itemsJson)
    }
}

// Data holder classes
data class CartSummary(
    val subtotal: Double,
    val discountAmount: Double,
    val gst: Double,
    val handlingCharge: Double,
    val totalSavings: Double,
    val totalAmount: Double
)

data class AdminStats(
    val totalOrders: Int,
    val deliveredOrdersCount: Int,
    val pendingOrdersCount: Int,
    val totalRevenue: Double,
    val lowStockCount: Int
)
