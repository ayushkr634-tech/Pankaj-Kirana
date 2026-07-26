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

    // --- Admin Platform States ---
    var adminIsLoggedIn = mutableStateOf(false)
    val categoriesList = androidx.compose.runtime.mutableStateListOf(
        "Rice & Flour", "Cooking Oil", "Spices",
        "Tea & Coffee", "Biscuits", "Snacks",
        "Instant Food", "Dairy Products", "Dry Fruits",
        "Chocolates", "Personal Care", "Cleaning Supplies"
    )
    val disabledProductIds = androidx.compose.runtime.mutableStateListOf<Int>()
    val bannersList = androidx.compose.runtime.mutableStateListOf(
        StoreBanner(1, "Flat 20% OFF", "Fresh Pulses & Staples", "Spices"),
        StoreBanner(2, "Buy 1 Get 1", "Select Premium Snacks", "Snacks")
    )
    val couponsList = androidx.compose.runtime.mutableStateListOf(
        StoreCoupon("WELCOME50", 15.0, "2026-12-31", 150.0),
        StoreCoupon("PANKAJ20", 20.0, "2026-08-15", 300.0),
        StoreCoupon("FESTIVE10", 10.0, "2026-09-30", 100.0)
    )
    val storeSettings = mutableStateOf(StoreSettings())
    val adminProfile = mutableStateOf(AdminProfile())
    var adminUsername = mutableStateOf("ayushkr@gmail.com")
    var adminPassword = mutableStateOf("020202")
    val orderAlertPrefsMap = androidx.compose.runtime.mutableStateMapOf<Int, OrderAlertPrefs>()
    val activeInAppStatusAlert = mutableStateOf<OrderEntity?>(null)
    val showOrderPlacedSuccessAlert = mutableStateOf(false)
    private val orderStatusCache = mutableMapOf<Int, String>()

    val blockedCustomerIds = androidx.compose.runtime.mutableStateListOf<Int>()
    val customersList = androidx.compose.runtime.mutableStateListOf(
        UserProfileEntity(id = 1, name = "Ayush Kumar", email = "ayushkr634@gmail.com", mobile = "8235091376", loyaltyPoints = 250, referralCode = "PKAYUSH99"),
        UserProfileEntity(id = 2, name = "Rahul Sharma", email = "rahul@gmail.com", mobile = "9876543210", loyaltyPoints = 120, referralCode = "PKRAHUL12"),
        UserProfileEntity(id = 3, name = "Priya Gupta", email = "priya@gmail.com", mobile = "9123456789", loyaltyPoints = 340, referralCode = "PKPRIYA77")
    )
    val notificationsList = androidx.compose.runtime.mutableStateListOf(
        PromoNotification(1, "Weekend Sale 🍉", "Fresh seasonal fruits are now at flat 15% discount!", System.currentTimeMillis() - 86400000),
        PromoNotification(2, "Monsoon Essentials ⛈️", "Stay protected with cleaning & hygiene specials.", System.currentTimeMillis() - 43200000)
    )

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

        // Listen to order updates to trigger status change alerts
        viewModelScope.launch {
            repository.allOrders.collect { orders ->
                orders.forEach { order ->
                    val oldStatus = orderStatusCache[order.id]
                    if (oldStatus != null && oldStatus != order.status) {
                        triggerStatusChangeAlerts(order, oldStatus)
                    }
                    orderStatusCache[order.id] = order.status
                }
            }
        }
    }

    // --- Translations Helper ---
    fun t(en: String, hi: String): String {
        return if (currentLanguage.value == "HI") hi else en
    }

    // --- Catalog Actions ---
    fun setProductCategory(category: String?) {
        selectedCategory.value = if (category == "All" || category == "सभी") null else category
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

        // Apply coupon discount (Dynamic from admin coupon settings)
        val coupon = couponsList.find { it.code.equals(appliedCouponCode.value, ignoreCase = true) }
        val discountAmount = if (coupon != null && subtotal >= coupon.minOrderAmount) {
            subtotal * (coupon.discountPercent / 100.0)
        } else {
            0.0
        }

        val gst = 0.0 // GST Removed

        val handlingCharge = storeSettings.value.deliveryCharge // Loaded from dynamic admin settings

        val total = (subtotal - discountAmount + gst + handlingCharge).coerceAtLeast(0.0)

        return CartSummary(
            subtotal = subtotal,
            discountAmount = discountAmount,
            gst = gst,
            handlingCharge = handlingCharge,
            totalSavings = totalSavings,
            totalAmount = total
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
                showOrderPlacedSuccessAlert.value = true
                currentScreen.value = "ORDER_TRACKING"
            }
        }
    }

    fun triggerStatusChangeAlerts(order: OrderEntity, oldStatus: String) {
        val prefs = orderAlertPrefsMap[order.id] ?: OrderAlertPrefs(order.id)
        
        // 1. In-App live overlay popup
        if (prefs.inAppStatusChangeAlert) {
            activeInAppStatusAlert.value = order
        }
        
        val context = getApplication<Application>().applicationContext
        
        // 2. Sound & Vibration Alert
        if (prefs.soundAndVibrationAlert) {
            try {
                // Vibrate
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(300)
                    }
                }
                
                // Play notification sound
                val ringtoneUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = android.media.RingtoneManager.getRingtone(context, ringtoneUri)
                ringtone?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // 3. System Notification Bar Alert
        if (prefs.systemNotificationAlert) {
            try {
                val channelId = "order_status_channel"
                val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(
                        channelId,
                        "Order Alerts",
                        android.app.NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notifications for order status changes"
                    }
                    notificationManager.createNotificationChannel(channel)
                }
                
                // Build and post notification
                val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Order #${order.id} Updated! 📦")
                    .setContentText("Status changed from '$oldStatus' to '${order.status}'")
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                
                notificationManager.notify(order.id, builder.build())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
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
        price: Double, discount: Double, stock: Int, description: String,
        imageUrl: String? = null, imageEmoji: String? = null
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
                isNewArrival = true,
                imageUrl = imageUrl,
                imageEmoji = imageEmoji
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

    // --- Dynamic Admin Panel Actions ---
    fun adminUpdateProduct(product: GroceryProduct) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun adminAddCategory(category: String) {
        if (category.isNotBlank() && !categoriesList.contains(category)) {
            categoriesList.add(category)
        }
    }

    fun adminEditCategory(oldCategory: String, newCategory: String) {
        if (newCategory.isNotBlank() && categoriesList.contains(oldCategory)) {
            val idx = categoriesList.indexOf(oldCategory)
            if (idx != -1) {
                categoriesList[idx] = newCategory
                viewModelScope.launch {
                    allProducts.value.filter { it.category == oldCategory }.forEach { prod ->
                        repository.updateProduct(prod.copy(category = newCategory))
                    }
                }
            }
        }
    }

    fun adminDeleteCategory(category: String) {
        if (categoriesList.contains(category)) {
            categoriesList.remove(category)
            viewModelScope.launch {
                allProducts.value.filter { it.category == category }.forEach { prod ->
                    repository.updateProduct(prod.copy(category = "Uncategorized"))
                }
            }
        }
    }

    fun adminAddBanner(title: String, promoText: String, categoryTarget: String) {
        val nextId = (bannersList.maxOfOrNull { it.id } ?: 0) + 1
        bannersList.add(StoreBanner(nextId, title, promoText, categoryTarget = categoryTarget))
    }

    fun adminEditBanner(id: Int, title: String, promoText: String, categoryTarget: String) {
        val idx = bannersList.indexOfFirst { it.id == id }
        if (idx != -1) {
            bannersList[idx] = StoreBanner(id, title, promoText, categoryTarget = categoryTarget)
        }
    }

    fun adminDeleteBanner(id: Int) {
        bannersList.removeAll { it.id == id }
    }

    fun adminAddCoupon(code: String, discountPercent: Double, expiryDate: String, minOrderAmount: Double) {
        if (code.isNotBlank() && !couponsList.any { it.code.equals(code, ignoreCase = true) }) {
            couponsList.add(StoreCoupon(code.uppercase(), discountPercent, expiryDate, minOrderAmount))
        }
    }

    fun adminEditCoupon(oldCode: String, newCode: String, discountPercent: Double, expiryDate: String, minOrderAmount: Double) {
        val idx = couponsList.indexOfFirst { it.code == oldCode }
        if (idx != -1) {
            couponsList[idx] = StoreCoupon(newCode.uppercase(), discountPercent, expiryDate, minOrderAmount)
        }
    }

    fun adminDeleteCoupon(code: String) {
        couponsList.removeAll { it.code == code }
    }

    fun adminToggleBlockCustomer(customerId: Int) {
        if (blockedCustomerIds.contains(customerId)) {
            blockedCustomerIds.remove(customerId)
        } else {
            blockedCustomerIds.add(customerId)
        }
    }

    fun adminSendNotification(title: String, message: String) {
        val nextId = (notificationsList.maxOfOrNull { it.id } ?: 0) + 1
        notificationsList.add(0, PromoNotification(nextId, title, message, System.currentTimeMillis()))
    }

    fun adminDeleteNotification(id: Int) {
        notificationsList.removeAll { it.id == id }
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

data class StoreBanner(
    val id: Int,
    val title: String,
    val promoText: String,
    val categoryTarget: String
)

data class StoreCoupon(
    val code: String,
    val discountPercent: Double,
    val expiryDate: String,
    val minOrderAmount: Double
)

data class StoreSettings(
    val storeName: String = "Pankaj Kirana",
    val storeLogoEmoji: String = "🏪",
    val contactNumber: String = "+91 82350 91376",
    val address: String = "Sipara Road, near Shiv Mandir, Sipara, Patna, Bihar - 800020",
    val deliveryCharge: Double = 10.0, // Represents handling charge
    val businessHours: String = "8:00 AM - 8:00 PM"
)

data class AdminProfile(
    val name: String = "Pankaj Kumar",
    val email: String = "pankaj.kirana@gmail.com",
    val mobile: String = "+91 99345 67890"
)

data class PromoNotification(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: Long
)

data class OrderAlertPrefs(
    val orderId: Int,
    val inAppStatusChangeAlert: Boolean = true,
    val soundAndVibrationAlert: Boolean = true,
    val systemNotificationAlert: Boolean = true,
    val whatsappAlertSim: Boolean = false,
    val smsAlertSim: Boolean = false
)
