package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "products")
@JsonClass(generateAdapter = true)
data class GroceryProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String,
    val category: String,
    val weight: String,
    val price: Double,
    val discount: Double, // percentage e.g. 10.0 for 10%
    val stock: Int,
    val description: String,
    val ingredients: String,
    val nutritionalInfo: String,
    val expiryDate: String,
    val rating: Double,
    val reviewCount: Int,
    val isFeatured: Boolean,
    val isBestSeller: Boolean,
    val isNewArrival: Boolean,
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantity: Int,
    val isSavedForLater: Boolean = false
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val productId: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val itemsJson: String, // Moshi-serialized list of OrderProductInfo
    val totalAmount: Double,
    val deliveryAddress: String,
    val deliveryInstructions: String,
    val deliveryTime: String,
    val paymentMethod: String,
    val status: String, // Pending, Packing, Out for Delivery, Delivered
    val estimatedDelivery: String
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val mobile: String,
    val loyaltyPoints: Int,
    val referralCode: String
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // e.g., "Home", "Office"
    val addressLine: String
)

// Helper class for JSON serialization of order items
@JsonClass(generateAdapter = true)
data class OrderProductInfo(
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val priceAtOrder: Double
)
