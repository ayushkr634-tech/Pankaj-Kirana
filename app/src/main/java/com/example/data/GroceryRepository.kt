package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GroceryRepository(private val dao: GroceryDao) {
    private val moshi = Moshi.Builder().build()
    private val orderListAdapter = moshi.adapter<List<OrderProductInfo>>(
        Types.newParameterizedType(List::class.java, OrderProductInfo::class.java)
    )

    val allProducts: Flow<List<GroceryProduct>> = dao.getAllProductsFlow()
    val cartItems: Flow<List<CartItem>> = dao.getCartItemsFlow()
    val wishlistItems: Flow<List<WishlistItem>> = dao.getWishlistItemsFlow()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrdersFlow()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfileFlow()
    val allAddresses: Flow<List<AddressEntity>> = dao.getAddressesFlow()

    suspend fun getProductById(id: Int): GroceryProduct? = dao.getProductById(id)

    suspend fun insertProduct(product: GroceryProduct) = dao.insertProduct(product)
    suspend fun updateProduct(product: GroceryProduct) = dao.updateProduct(product)
    suspend fun deleteProduct(product: GroceryProduct) = dao.deleteProduct(product)

    suspend fun insertCartItem(cartItem: CartItem) = dao.insertCartItem(cartItem)
    suspend fun updateCartItem(cartItem: CartItem) = dao.updateCartItem(cartItem)
    suspend fun deleteCartItem(cartItem: CartItem) = dao.deleteCartItem(cartItem)
    suspend fun clearCart() = dao.clearCart()

    suspend fun insertWishlistItem(wishlistItem: WishlistItem) = dao.insertWishlistItem(wishlistItem)
    suspend fun deleteWishlistItem(productId: Int) = dao.deleteWishlistItem(WishlistItem(productId))

    suspend fun insertOrder(order: OrderEntity) = dao.insertOrder(order)
    suspend fun updateOrder(order: OrderEntity) = dao.updateOrder(order)

    suspend fun saveUserProfile(profile: UserProfileEntity) = dao.insertUserProfile(profile)

    suspend fun insertAddress(address: AddressEntity) = dao.insertAddress(address)
    suspend fun deleteAddress(address: AddressEntity) = dao.deleteAddress(address)

    // Helper to serialize list of items to string
    fun serializeOrderItems(items: List<OrderProductInfo>): String {
        return orderListAdapter.toJson(items)
    }

    // Helper to deserialize list of items from string
    fun deserializeOrderItems(json: String): List<OrderProductInfo> {
        return try {
            orderListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Seeding method to run on initialization
    suspend fun seedDatabaseIfNeeded() {
        val currentProducts = dao.getAllProductsFlow().firstOrNull() ?: emptyList()
        if (currentProducts.isEmpty()) {
            val initialProducts = listOf(
                GroceryProduct(
                    name = "Aashirvaad Shudh Chakki Atta",
                    brand = "Aashirvaad",
                    category = "Rice & Flour",
                    weight = "5 kg",
                    price = 260.0,
                    discount = 5.0,
                    stock = 45,
                    description = "High-quality 100% pure stone-ground whole wheat flour. Perfect for making soft, fluffy, and healthy chapatis.",
                    ingredients = "Whole Wheat Grains",
                    nutritionalInfo = "Energy: 341 kcal, Carbohydrates: 73g, Protein: 12g, Fat: 1.7g per 100g",
                    expiryDate = "Best before 3 months from packaging",
                    rating = 4.8,
                    reviewCount = 128,
                    isFeatured = true,
                    isBestSeller = true,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "India Gate Basmati Rice (Premium)",
                    brand = "India Gate",
                    category = "Rice & Flour",
                    weight = "1 kg",
                    price = 145.0,
                    discount = 10.0,
                    stock = 30,
                    description = "Aged premium long grain basmati rice, highly aromatic and slender-grained. Ideal for biryanis, pulaos, and special occasions.",
                    ingredients = "Basmati Rice Grains",
                    nutritionalInfo = "Energy: 350 kcal, Protein: 8.5g, Carbohydrates: 78g per 100g",
                    expiryDate = "Best before 24 months from packaging",
                    rating = 4.7,
                    reviewCount = 94,
                    isFeatured = true,
                    isBestSeller = true,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Fortune Pure Mustard Oil",
                    brand = "Fortune",
                    category = "Cooking Oil",
                    weight = "1 L",
                    price = 175.0,
                    discount = 12.0,
                    stock = 50,
                    description = "Cold pressed raw mustard oil with strong flavor and high pungency. Retains natural antioxidants and nutritional values.",
                    ingredients = "Cold Pressed Mustard Grains",
                    nutritionalInfo = "Energy: 900 kcal, Fats: 100g, Saturated Fat: 12g, Vitamin E: 30mg per 100g",
                    expiryDate = "Best before 12 months from packaging",
                    rating = 4.6,
                    reviewCount = 112,
                    isFeatured = true,
                    isBestSeller = false,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Tata Salt (Iodized)",
                    brand = "Tata",
                    category = "Spices",
                    weight = "1 kg",
                    price = 28.0,
                    discount = 0.0,
                    stock = 120,
                    description = "Vacuum-evaporated iodized salt ensuring mental development in kids and overall health.",
                    ingredients = "Edible Common Salt, Potassium Iodate",
                    nutritionalInfo = "Sodium: 38.7g, Iodine: 15 ppm per 100g",
                    expiryDate = "Best before 36 months from packaging",
                    rating = 4.9,
                    reviewCount = 250,
                    isFeatured = false,
                    isBestSeller = true,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Tata Tea Premium",
                    brand = "Tata",
                    category = "Tea & Coffee",
                    weight = "500 g",
                    price = 180.0,
                    discount = 8.0,
                    stock = 40,
                    description = "A unique blend of fine tea leaves and premium long leaves that delivers an exquisite taste, strong flavor, and rich color.",
                    ingredients = "Assam CTC Tea, Premium Green Leaves",
                    nutritionalInfo = "Energy: 100 kcal, Tea Polyphenols: 12% per 100g",
                    expiryDate = "Best before 12 months from packaging",
                    rating = 4.5,
                    reviewCount = 82,
                    isFeatured = true,
                    isBestSeller = true,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Catch Turmeric Powder (Haldi)",
                    brand = "Catch",
                    category = "Spices",
                    weight = "200 g",
                    price = 52.0,
                    discount = 5.0,
                    stock = 65,
                    description = "Sourced from high-quality turmeric fingers. Adds vibrant golden color and earthy aroma to Indian curries.",
                    ingredients = "Ground Turmeric Roots",
                    nutritionalInfo = "Curcumin content: min 3%, Energy: 350 kcal per 100g",
                    expiryDate = "Best before 12 months from packaging",
                    rating = 4.6,
                    reviewCount = 47,
                    isFeatured = false,
                    isBestSeller = false,
                    isNewArrival = true
                ),
                GroceryProduct(
                    name = "Haldiram's Bhujia Sev",
                    brand = "Haldiram's",
                    category = "Snacks",
                    weight = "350 g",
                    price = 110.0,
                    discount = 15.0,
                    stock = 75,
                    description = "Crunchy and mildly spicy Indian savory noodle snack made from dew bean flour (moth flour) and chickpea flour.",
                    ingredients = "Moth Pulse Flour, Chickpea Flour, Edible Vegetable Oil, Mixed Spices",
                    nutritionalInfo = "Energy: 578 kcal, Protein: 10g, Carbohydrates: 40g, Fats: 42g per 100g",
                    expiryDate = "Best before 6 months from packaging",
                    rating = 4.8,
                    reviewCount = 165,
                    isFeatured = true,
                    isBestSeller = true,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Maggi 2-Minute Masala Noodles",
                    brand = "Maggi",
                    category = "Instant Food",
                    weight = "280 g",
                    price = 48.0,
                    discount = 5.0,
                    stock = 100,
                    description = "India's beloved noodles, made with the perfect blend of 12 roasted spices for a delicious quick snack.",
                    ingredients = "Wheat Flour, Palm Oil, Salt, Mixed Spices (Coriander, Turmeric, Cumin, Ginger, Garlic)",
                    nutritionalInfo = "Energy: 389 kcal, Protein: 8.2g, Carbohydrates: 59g, Fat: 13g per 100g",
                    expiryDate = "Best before 9 months from packaging",
                    rating = 4.7,
                    reviewCount = 210,
                    isFeatured = true,
                    isBestSeller = true,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Amul Butter (Salted)",
                    brand = "Amul",
                    category = "Dairy Products",
                    weight = "100 g",
                    price = 56.0,
                    discount = 3.0,
                    stock = 80,
                    description = "Utterly butterly delicious! Pure salted cream butter, a breakfast staple across Indian households.",
                    ingredients = "Butterfat, Moisture, Salt, Curd",
                    nutritionalInfo = "Energy: 722 kcal, Fat: 80g, Protein: 0.5g per 100g",
                    expiryDate = "Best before 12 months from packaging (keep refrigerated)",
                    rating = 4.9,
                    reviewCount = 310,
                    isFeatured = true,
                    isBestSeller = true,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Amul Taaza Fresh Milk",
                    brand = "Amul",
                    category = "Dairy Products",
                    weight = "1 L",
                    price = 66.0,
                    discount = 0.0,
                    stock = 90,
                    description = "Homogenized toned milk. Contains essential calcium and proteins for active development.",
                    ingredients = "Toned Milk, Vitamin A, Vitamin D",
                    nutritionalInfo = "Energy: 58 kcal, Protein: 3.2g, Calcium: 120mg per 100ml",
                    expiryDate = "Use within 2 days of packaging (keep refrigerated)",
                    rating = 4.8,
                    reviewCount = 188,
                    isFeatured = false,
                    isBestSeller = true,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Dettol Liquid Handwash",
                    brand = "Dettol",
                    category = "Personal Care",
                    weight = "200 ml",
                    price = 99.0,
                    discount = 10.0,
                    stock = 45,
                    description = "Provides 100% better protection against illness-causing germs. Active skin-conditioning formula.",
                    ingredients = "Salicylic Acid, Glycerin, Sodium Laureth Sulfate",
                    nutritionalInfo = "pH Balanced, Germ Protection Formula",
                    expiryDate = "Best before 24 months from packaging",
                    rating = 4.6,
                    reviewCount = 65,
                    isFeatured = false,
                    isBestSeller = false,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Surf Excel Easy Wash Detergent",
                    brand = "Surf Excel",
                    category = "Cleaning Supplies",
                    weight = "1 kg",
                    price = 140.0,
                    discount = 12.0,
                    stock = 55,
                    description = "Specially formulated to remove tough grease, oil, and food stains with ease while keeping fabric colors vibrant.",
                    ingredients = "Anionic Surfactants, Sodium Carbonate, Bleach, Optical Brighteners",
                    nutritionalInfo = "Stain-fighting formulation",
                    expiryDate = "Best before 36 months from packaging",
                    rating = 4.7,
                    reviewCount = 74,
                    isFeatured = false,
                    isBestSeller = false,
                    isNewArrival = false
                ),
                GroceryProduct(
                    name = "Premium California Almonds",
                    brand = "Pankaj Premium",
                    category = "Dry Fruits",
                    weight = "250 g",
                    price = 280.0,
                    discount = 20.0,
                    stock = 35,
                    description = "Premium selected California almonds. Rich in Vitamin E, dietary fiber, and healthy monounsaturated fats. Excellent energy booster.",
                    ingredients = "100% Natural Almonds",
                    nutritionalInfo = "Energy: 579 kcal, Protein: 21g, Fat: 49g per 100g",
                    expiryDate = "Best before 6 months from packaging",
                    rating = 4.8,
                    reviewCount = 52,
                    isFeatured = true,
                    isBestSeller = false,
                    isNewArrival = true
                ),
                GroceryProduct(
                    name = "Cadbury Dairy Milk Silk",
                    brand = "Cadbury",
                    category = "Chocolates",
                    weight = "60 g",
                    price = 80.0,
                    discount = 5.0,
                    stock = 60,
                    description = "Rich, smooth, and creamy milk chocolate. Perfect treat to sweeten your celebrations and satisfy sweet cravings.",
                    ingredients = "Sugar, Cocoa Butter, Milk Solids, Cocoa Solids",
                    nutritionalInfo = "Energy: 532 kcal, Protein: 6.3g, Carbohydrates: 57.5g per 100g",
                    expiryDate = "Best before 12 months from packaging",
                    rating = 4.8,
                    reviewCount = 98,
                    isFeatured = false,
                    isBestSeller = true,
                    isNewArrival = false
                )
            )
            dao.insertProducts(initialProducts)
        }

        // Seed profile if empty
        val currentProfile = dao.getUserProfileFlow().firstOrNull()
        if (currentProfile == null) {
            val defaultProfile = UserProfileEntity(
                id = 1,
                name = "Ayush Kumar",
                email = "ayushkr634@gmail.com",
                mobile = "8235091376",
                loyaltyPoints = 250,
                referralCode = "PKAYUSH99"
            )
            dao.insertUserProfile(defaultProfile)
        }

        // Seed a default address if empty
        val currentAddresses = dao.getAddressesFlow().firstOrNull() ?: emptyList()
        if (currentAddresses.isEmpty()) {
            dao.insertAddress(AddressEntity(title = "Home", addressLine = "H No. 12, Sipara Road, near Shiv Mandir, Sipara, Patna, Bihar - 800020"))
            dao.insertAddress(AddressEntity(title = "Office", addressLine = "Pankaj Kirana Store, Main Chowk, Sipara, Patna, Bihar - 800020"))
        }
    }
}
