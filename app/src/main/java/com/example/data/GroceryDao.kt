package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    // Products
    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<GroceryProduct>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): GroceryProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<GroceryProduct>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: GroceryProduct)

    @Update
    suspend fun updateProduct(product: GroceryProduct)

    @Delete
    suspend fun deleteProduct(product: GroceryProduct)

    // Cart
    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Wishlist
    @Query("SELECT * FROM wishlist_items")
    fun getWishlistItemsFlow(): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(wishlistItem: WishlistItem)

    @Delete
    suspend fun deleteWishlistItem(wishlistItem: WishlistItem)

    // Orders
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    // Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // Addresses
    @Query("SELECT * FROM addresses")
    fun getAddressesFlow(): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)
}
