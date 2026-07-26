package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.ui.CartSummary
import com.example.ui.GroceryViewModel
import com.example.ui.components.StarRatingBar
import java.text.SimpleDateFormat
import java.util.*

private fun getCategoryEmoji(category: String, isHindi: Boolean): String {
    return when {
        category.contains("All") || category.contains("सभी") -> "🛍️"
        category.contains("Rice") || category.contains("Flour") -> "🌾"
        category.contains("Oil") -> "🧴"
        category.contains("Spices") -> "🫘"
        category.contains("Tea") || category.contains("Coffee") -> "☕"
        category.contains("Biscuits") -> "🍪"
        category.contains("Snacks") -> "🍿"
        category.contains("Instant") -> "🍜"
        category.contains("Dairy") -> "🥛"
        category.contains("Dry Fruits") -> "🥜"
        category.contains("Chocolates") -> "🍫"
        category.contains("Personal") -> "🧼"
        category.contains("Cleaning") -> "🧹"
        else -> "📦"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: GroceryViewModel,
    products: List<GroceryProduct>,
    cart: List<CartItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFilterSheet by remember { mutableStateOf(false) }
    var voiceDialogOpen by remember { mutableStateOf(false) }
    var barcodeDialogOpen by remember { mutableStateOf(false) }

    val categoriesList = listOf(viewModel.t("All", "सभी")) + viewModel.categoriesList

    // Dynamic Filter & Sort Logic applied on products
    val filteredProducts = remember(products, viewModel.searchQuery.value, viewModel.selectedCategory.value, viewModel.sortOption.value, viewModel.disabledProductIds.size) {
        var list = products.filter { product ->
            !viewModel.disabledProductIds.contains(product.id) && (
                product.name.contains(viewModel.searchQuery.value, ignoreCase = true) ||
                product.brand.contains(viewModel.searchQuery.value, ignoreCase = true) ||
                product.category.contains(viewModel.searchQuery.value, ignoreCase = true)
            )
        }.filter { product ->
            viewModel.selectedCategory.value == null || product.category == viewModel.selectedCategory.value
        }

        list = when (viewModel.sortOption.value) {
            "Low to High" -> list.sortedBy { it.price * (1 - (it.discount / 100.0)) }
            "High to Low" -> list.sortedByDescending { it.price * (1 - (it.discount / 100.0)) }
            "Best Rated" -> list.sortedByDescending { it.rating }
            else -> list // "Popular" / Default
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("customer_home_scroll")
    ) {
        // --- 1. Sticky Store Banner ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                // Static generated JPG header
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Pankaj Kirana Store Front",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Earthy overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                startY = 50f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Pankaj Kirana",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = viewModel.t("Your Trusted Neighborhood Grocery Store", "आपकी अपनी भरोसेमंद किराने की दुकान"),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- 2. Live Search Bar & Quick Voice / Barcode Scan ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (MaterialTheme.colorScheme.primary == Color(0xFF1B6C31)) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    TextField(
                        value = viewModel.searchQuery.value,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text(viewModel.t("Search 2000+ products...", "2000+ उत्पादों में खोजें..."), fontSize = 12.sp, color = Color(0xFF414941)) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_search_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true
                    )
                    
                    // Voice Search Dialog trigger
                    IconButton(
                        onClick = { voiceDialogOpen = true },
                        modifier = Modifier.testTag("voice_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Barcode Scanner trigger
                    IconButton(
                        onClick = { barcodeDialogOpen = true },
                        modifier = Modifier.testTag("barcode_scanner_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Filters Sheet Trigger
                    IconButton(
                        onClick = { showFilterSheet = !showFilterSheet },
                        modifier = Modifier.testTag("filter_sort_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter and Sort",
                            tint = if (showFilterSheet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // --- 2.5. High Density Dynamic Promo Banners ---
        if (viewModel.bannersList.isNotEmpty()) {
            items(viewModel.bannersList) { banner ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD0EBCB)),
                    border = BorderStroke(1.dp, Color(0xFFBFC9BF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .align(Alignment.CenterStart)
                        ) {
                            Text(
                                text = banner.title,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B6C31),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = banner.promoText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00210B),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.setProductCategory(banner.categoryTarget) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1B6C31),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(viewModel.t("SHOP NOW", "अभी खरीदें"), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Background ambient blur design representation using a soft green circle
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.CenterEnd)
                                .graphicsLayer(alpha = 0.25f)
                                .background(Color(0xFF1B6C31), CircleShape)
                        )
                    }
                }
            }
        }

        // --- Filter Expandable Box Panel ---
        item {
            AnimatedVisibility(
                visible = showFilterSheet,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = viewModel.t("Sort By Price / Rating", "कीमत / रेटिंग के अनुसार क्रमबद्ध करें"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val options = listOf("Popular", "Low to High", "High to Low", "Best Rated")
                            options.forEach { opt ->
                                val isSel = viewModel.sortOption.value == opt
                                FilterChip(
                                    selected = isSel,
                                    onClick = { viewModel.sortOption.value = opt },
                                    label = { Text(viewModel.t(opt, when(opt) {
                                        "Low to High" -> "कम से ज्यादा"
                                        "High to Low" -> "ज्यादा से कम"
                                        "Best Rated" -> "सर्वश्रेष्ठ रेटिंग"
                                        else -> "लोकप्रिय"
                                    })) },
                                    modifier = Modifier.testTag("sort_chip_$opt")
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Categories Horizontal Bar ---
        item {
            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = viewModel.t("Browse Categories", "श्रेणियां ब्राउज़ करें"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categoriesList) { cat ->
                        val isSelected = (viewModel.selectedCategory.value == cat || (cat == viewModel.t("All", "सभी") && viewModel.selectedCategory.value == null))
                        val isHindi = viewModel.currentLanguage.value == "HI"
                        val emoji = getCategoryEmoji(cat, isHindi)

                        Card(
                            onClick = { viewModel.setProductCategory(cat) },
                            modifier = Modifier
                                .width(76.dp)
                                .height(66.dp)
                                .testTag("category_chip_$cat"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cat,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Today's Offers / Featured Carousel (if no category is selected) ---
        if (viewModel.selectedCategory.value == null && viewModel.searchQuery.value.isEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.t("Today's Super Offers 🏷️", "आज के शानदार ऑफर्स 🏷️"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = viewModel.t("Save Big", "बचत करें"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val offerProducts = products.filter { it.discount >= 10.0 }
                        items(offerProducts) { prod ->
                            OfferProductCard(product = prod, onSelect = {
                                viewModel.selectedProductId.value = prod.id
                                viewModel.currentScreen.value = "PRODUCT_DETAILS"
                            }, onAddToCart = {
                                viewModel.addToCart(prod.id)
                                Toast.makeText(context, "${prod.name} added to cart", Toast.LENGTH_SHORT).show()
                            })
                        }
                    }
                }
            }
        }

        // --- 5. Main Grid Items Section Header ---
        item {
            Text(
                text = if (viewModel.selectedCategory.value != null) {
                    viewModel.selectedCategory.value!!
                } else {
                    viewModel.t("All Fresh Staples", "सभी ताजा सामग्री")
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // --- 6. Product Cards Grid Items ---
        if (filteredProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "No Products",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.t("No products found in this range", "इस श्रेणी में कोई उत्पाद नहीं मिला"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Displaying products in structured rows instead of raw Grid layout for best Scroll control inside LazyColumn
            val rows = filteredProducts.chunked(3)
            items(rows, key = { row -> row.map { it.id }.joinToString(",") }) { triple ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until 3) {
                        if (i < triple.size) {
                            val prod = triple[i]
                            ProductItemCard(
                                product = prod,
                                modifier = Modifier.weight(1f),
                                onSelect = {
                                    viewModel.selectedProductId.value = prod.id
                                    viewModel.currentScreen.value = "PRODUCT_DETAILS"
                                },
                                onAddToCart = {
                                    viewModel.addToCart(prod.id)
                                    Toast.makeText(context, "${prod.name} added!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // --- Voice Assistant Simulation Dialog ---
    if (voiceDialogOpen) {
        AlertDialog(
            onDismissRequest = { voiceDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendAiMessage("Suggest some pulses or flour options for a healthy diet")
                        viewModel.currentScreen.value = "SUPPORT"
                        voiceDialogOpen = false
                    },
                    modifier = Modifier.testTag("voice_confirm_btn")
                ) {
                    Text(viewModel.t("Send to AI Assistant", "AI सहायक को भेजें"))
                }
            },
            dismissButton = {
                TextButton(onClick = { voiceDialogOpen = false }) {
                    Text(viewModel.t("Cancel", "रद्द करें"))
                }
            },
            title = { Text(viewModel.t("Voice Shopping Mode 🎙️", "आवाज से खरीदारी मोड 🎙️")) },
            text = {
                Column {
                    Text(
                        viewModel.t("Speak now or type your grocery query...", "अभी बोलें या अपनी किराने की मांग टाइप करें..."),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        viewModel.t("Example: 'I want to bake fresh roti, what do I need?'", "उदाहरण: 'मुझे ताज़ा रोटी बनानी है, क्या चाहिए?'"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // --- Barcode / QR Scan Simulation Dialog ---
    if (barcodeDialogOpen) {
        val scannableProduct = products.firstOrNull { it.isBestSeller } ?: products.first()
        AlertDialog(
            onDismissRequest = { barcodeDialogOpen = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.searchQuery.value = scannableProduct.name
                        Toast.makeText(context, "Scanned: ${scannableProduct.name}", Toast.LENGTH_SHORT).show()
                        barcodeDialogOpen = false
                    },
                    modifier = Modifier.testTag("scan_simulate_btn")
                ) {
                    Text(viewModel.t("Simulate Scan Match", "स्कैन मैच अनुकरण करें"))
                }
            },
            dismissButton = {
                TextButton(onClick = { barcodeDialogOpen = false }) {
                    Text(viewModel.t("Cancel", "रद्द करें"))
                }
            },
            title = { Text(viewModel.t("Scan Barcode / QR Code 📷", "बारकोड / क्यूआर कोड स्कैन 📷")) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.t("Aim the camera at the product package's barcode.", "कैमरे को उत्पाद के बारकोड पर केंद्रित करें।"))
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Barcode Target",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Target Match: ${scannableProduct.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Custom Product Item Card
@Composable
fun ProductItemCard(
    product: GroceryProduct,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
    onAddToCart: () -> Unit
) {
    val finalPrice = product.price * (1 - (product.discount / 100.0))
    val isOutOfStock = product.stock == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Image / Category Section styled with the light background and rounded borders of the theme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                    )
                } else if (!product.imageEmoji.isNullOrBlank()) {
                    Text(text = product.imageEmoji, fontSize = 28.sp)
                } else {
                    // Vector / Placeholder Icon representation for product category
                    Icon(
                        imageVector = when (product.category) {
                            "Rice & Flour" -> Icons.Default.Eco
                            "Cooking Oil" -> Icons.Default.Opacity
                            "Spices" -> Icons.Default.AllInclusive
                            "Tea & Coffee" -> Icons.Default.LocalCafe
                            "Biscuits", "Snacks" -> Icons.Default.BreakfastDining
                            "Dairy Products" -> Icons.Default.WaterDrop
                            "Dry Fruits" -> Icons.Default.Grain
                            "Chocolates" -> Icons.Default.Cake
                            "Personal Care" -> Icons.Default.Face
                            "Cleaning Supplies" -> Icons.Default.CleanHands
                            else -> Icons.Default.ShoppingBasket
                        },
                        contentDescription = product.name,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )

                    // Large Category Emoji in background for premium styling matching HTML
                    Text(
                        text = when (product.category) {
                            "Rice & Flour" -> "🌾"
                            "Cooking Oil" -> "🧴"
                            "Spices" -> "🫘"
                            "Tea & Coffee" -> "☕"
                            "Biscuits", "Snacks" -> "🍪"
                            "Dairy Products" -> "🥛"
                            "Dry Fruits" -> "🥜"
                            "Chocolates" -> "🍫"
                            "Personal Care" -> "🧼"
                            "Cleaning Supplies" -> "🧹"
                            else -> "📦"
                        },
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .graphicsLayer(alpha = 0.25f)
                    )
                }

                if (product.discount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(bottomEnd = 6.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${product.discount.toInt()}% OFF",
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                Text(
                    text = product.brand.uppercase(),
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.weight,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(1.dp))

                // Price display with strong typography
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "₹${finalPrice.toInt()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.discount > 0) {
                        Text(
                            text = "₹${product.price.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Stock status & Rating line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300), // Rich gold star
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = product.rating.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                when {
                                    isOutOfStock -> Color.Red.copy(alpha = 0.1f)
                                    product.stock < 10 -> Color(0xFFFFF9C4) // Soft yellow bg
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                },
                                RoundedCornerShape(3.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = when {
                                isOutOfStock -> "Sold Out"
                                product.stock < 10 -> "Only ${product.stock} Left"
                                else -> "In Stock"
                            },
                            color = when {
                                isOutOfStock -> Color.Red
                                product.stock < 10 -> Color(0xFFF57C00)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Add to cart action button matching HTML (secondary light green background and compact feel)
                Button(
                    onClick = onAddToCart,
                    enabled = !isOutOfStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .testTag("add_to_cart_${product.id}"),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("ADD", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}

// Custom Offer Card
@Composable
fun OfferProductCard(
    product: GroceryProduct,
    onSelect: () -> Unit,
    onAddToCart: () -> Unit
) {
    val finalPrice = product.price * (1 - (product.discount / 100.0))
    
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onSelect)
            .testTag("offer_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(bottomStart = 4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("${product.discount.toInt()}% OFF", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(product.weight, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("₹${finalPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text("₹${product.price.toInt()}", style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough), color = Color.Gray, fontSize = 9.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth().height(22.dp).testTag("offer_add_${product.id}"),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Grab Offer", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PRODUCT DETAILS VIEW ---
@Composable
fun ProductDetailsView(
    viewModel: GroceryViewModel,
    productId: Int,
    products: List<GroceryProduct>
) {
    val context = LocalContext.current
    val product = products.find { it.id == productId } ?: return
    val finalPrice = product.price * (1 - (product.discount / 100.0))
    val isOutOfStock = product.stock == 0

    // Local Review submission state
    var reviewText by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5.0) }
    val localReviews = remember {
        mutableStateListOf(
            Pair("Rahul Sharma", "Super fresh flour! Roti stays soft for hours. Highly recommended!"),
            Pair("Priya Gupta", "Standard Tata tea, great aroma. Packing was fully sealed."),
            Pair("Vikram Kumar", "Got a 12% discount, super fast delivery by Pankaj Kirana.")
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(product.name) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = "HOME" }, modifier = Modifier.testTag("details_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleWishlist(product.id) }) {
                        val isFav = viewModel.wishlistItems.value.any { it.productId == product.id }
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isFav) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Product Hero Header Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                    )
                } else if (!product.imageEmoji.isNullOrBlank()) {
                    Text(text = product.imageEmoji, fontSize = 96.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingBasket,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
                if (product.discount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("${product.discount.toInt()}% DISCOUNT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.brand, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(product.weight, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                
                Spacer(modifier = Modifier.height(12.dp))

                // Price Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${finalPrice.toInt()}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    if (product.discount > 0) {
                        Text("₹${product.price.toInt()}", style = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough), color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stock Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(if (isOutOfStock) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isOutOfStock) "Sold Out" else "In Stock (${product.stock} bags/items left)",
                            color = if (isOutOfStock) Color.Red else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.Star, contentDescription = "Stars", tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${product.rating} (${product.reviewCount} customer reviews)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Description & Details
                Text("Product Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(12.dp))

                Text("Ingredients", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.ingredients, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(12.dp))

                Text("Nutritional Information", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.nutritionalInfo, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(12.dp))

                Text("Expiry Date", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.expiryDate, color = Color.Red, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(16.dp))

                // Add to Cart Large CTA
                Button(
                    onClick = {
                        viewModel.addToCart(product.id)
                        Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                    },
                    enabled = !isOutOfStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("details_add_to_cart_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Fresh Pack to Cart", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Customer reviews section
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Customer Reviews", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Submit Review Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Write your review", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        StarRatingBar(rating = reviewRating, onRatingSelected = { reviewRating = it })
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = { Text("How was the quality, packing and delivery?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("review_input_field")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (reviewText.isNotBlank()) {
                                    localReviews.add(0, Pair("You", reviewText))
                                    reviewText = ""
                                    Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.align(Alignment.End).testTag("submit_review_btn")
                        ) {
                            Text("Submit Review")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Displaying Reviews
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    localReviews.forEach { (user, comment) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(user, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                                        Text("5.0", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(comment, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SHOPPING CART VIEW ---
@Composable
fun ShoppingCartView(
    viewModel: GroceryViewModel,
    productsList: List<GroceryProduct>,
    cartItems: List<CartItem>
) {
    val context = LocalContext.current
    val activeCart = cartItems.filter { !it.isSavedForLater }
    val savedCart = cartItems.filter { it.isSavedForLater }
    val summary = viewModel.getCartSummary(productsList, cartItems)

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(viewModel.t("Your Shopping Cart", "आपकी शॉपिंग कार्ट")) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = "HOME" }, modifier = Modifier.testTag("cart_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (activeCart.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(viewModel.t("Your cart is empty!", "आपकी कार्ट खाली है!"), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { viewModel.currentScreen.value = "HOME" }) {
                            Text(viewModel.t("Start Shopping", "खरीदारी शुरू करें"))
                        }
                    }
                }
            } else {
                // Cart Products List
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeCart.forEach { item ->
                        val prod = productsList.find { it.id == item.productId }
                        if (prod != null) {
                            CartProductRow(
                                product = prod,
                                item = item,
                                onPlus = { viewModel.addToCart(prod.id, 1) },
                                onMinus = { viewModel.decreaseCartQty(prod.id) },
                                onRemove = { viewModel.removeFromCart(prod.id) },
                                onSaveForLater = { viewModel.saveForLater(prod.id, true) }
                            )
                        }
                    }
                }



                // Price Invoice Summary Block
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(viewModel.t("Price Details", "मूल्य विवरण"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Items Subtotal")
                            Text("₹${summary.subtotal.toInt()}")
                        }
                        if (summary.discountAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Coupon Discount", color = MaterialTheme.colorScheme.primary)
                                Text("- ₹${summary.discountAmount.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Handling Charge")
                            Text("₹${summary.handlingCharge.toInt()}", color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount Payable", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("₹${summary.totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        if (summary.totalSavings > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Yay! You save ₹${summary.totalSavings.toInt()} on this order!",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // CTA to Checkout Screen
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.currentScreen.value = "CHECKOUT" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp)
                        .testTag("proceed_checkout_btn")
                ) {
                    Text(viewModel.t("Proceed to Checkout", "चेकआउट के लिए आगे बढ़ें"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // --- SAVE FOR LATER Shelf ---
            if (savedCart.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    viewModel.t("Saved For Later 📦", "बाद के लिए सहेजा गया 📦"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedCart.forEach { item ->
                        val prod = productsList.find { it.id == item.productId }
                        if (prod != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Price: ₹${(prod.price * (1 - (prod.discount/100.0))).toInt()} | ${prod.weight}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Row {
                                        TextButton(onClick = { viewModel.saveForLater(prod.id, false) }) {
                                            Text("Move to Cart")
                                        }
                                        IconButton(onClick = { viewModel.removeFromCart(prod.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Custom Cart item Product Row
@Composable
fun CartProductRow(
    product: GroceryProduct,
    item: CartItem,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    onRemove: () -> Unit,
    onSaveForLater: () -> Unit
) {
    val finalPrice = product.price * (1 - (product.discount / 100.0))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_row_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon representation
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Info middle
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("₹${finalPrice.toInt()} per ${product.weight}", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onSaveForLater,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(20.dp)
                    ) {
                        Text("Save for later", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(
                        onClick = onRemove,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(20.dp)
                    ) {
                        Text("Remove", fontSize = 11.sp, color = Color.Red)
                    }
                }
            }

            // Quantity adjusters right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onMinus,
                    modifier = Modifier.size(32.dp).testTag("qty_minus_${product.id}")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                }
                Text(
                    text = item.quantity.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = onPlus,
                    modifier = Modifier.size(32.dp).testTag("qty_plus_${product.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// --- CHECKOUT VIEW ---
@Composable
fun CheckoutView(
    viewModel: GroceryViewModel,
    productsList: List<GroceryProduct>,
    cartItems: List<CartItem>,
    addresses: List<AddressEntity>
) {
    val context = LocalContext.current
    val summary = viewModel.getCartSummary(productsList, cartItems)

    var instructionsInput by remember { mutableStateOf("Leave near door, ring bell") }
    var newAddressTitle by remember { mutableStateOf("") }
    var newAddressLine by remember { mutableStateOf("") }
    var isAddingAddress by remember { mutableStateOf(false) }
    var customLocation by remember { mutableStateOf("") }
    var customPhone by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(viewModel.t("Secure Checkout", "सुरक्षित चेकआउट")) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = "CART" }, modifier = Modifier.testTag("checkout_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Delivery Address Selector Section
            Text(viewModel.t("1. Select Delivery Address", "1. डिलीवरी का पता चुनें"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            addresses.forEach { addr ->
                val isSelected = viewModel.currentAddressSelectionId.value == addr.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.currentAddressSelectionId.value = addr.id }
                        .testTag("address_card_${addr.id}"),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.currentAddressSelectionId.value = addr.id }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(addr.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(addr.addressLine, fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.deleteAddress(addr) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Address", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Quick Inline Address Adder Form
            if (!isAddingAddress) {
                TextButton(onClick = { isAddingAddress = true }, modifier = Modifier.testTag("add_new_addr_btn")) {
                    Icon(Icons.Default.AddLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New Delivery Location")
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Add New Location Info", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newAddressTitle,
                            onValueChange = { newAddressTitle = it },
                            label = { Text("Label (e.g. Home, Office)") },
                            modifier = Modifier.fillMaxWidth().testTag("addr_title_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newAddressLine,
                            onValueChange = { newAddressLine = it },
                            label = { Text("Full Address Details") },
                            modifier = Modifier.fillMaxWidth().testTag("addr_line_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.align(Alignment.End)) {
                            TextButton(onClick = { isAddingAddress = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newAddressTitle.isNotBlank() && newAddressLine.isNotBlank()) {
                                        viewModel.addAddress(newAddressTitle, newAddressLine)
                                        newAddressTitle = ""
                                        newAddressLine = ""
                                        isAddingAddress = false
                                        Toast.makeText(context, "Location Saved!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("addr_save_btn")
                            ) {
                                Text("Save Location")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery Option Method
            Text(viewModel.t("2. Delivery Priority Options", "2. डिलीवरी प्राथमिकता विकल्प"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val methods = listOf("Home Delivery", "Express Delivery", "Store Pickup")
                methods.forEach { met ->
                    val isSelected = viewModel.checkoutDeliveryMethod.value == met
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent)
                            .clickable { viewModel.checkoutDeliveryMethod.value = met }
                            .padding(10.dp)
                            .testTag("delivery_method_$met"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (met) {
                                    "Express Delivery" -> Icons.Default.ElectricBolt
                                    "Store Pickup" -> Icons.Default.Storefront
                                    else -> Icons.Default.LocalShipping
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.t(met, when(met) {
                                    "Express Delivery" -> "एक्सप्रेस डिलीवरी"
                                    "Store Pickup" -> "स्टोर पिकअप"
                                    else -> "होम डिलीवरी"
                                }),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery instructions input
            OutlinedTextField(
                value = instructionsInput,
                onValueChange = { instructionsInput = it },
                label = { Text("Delivery Driver Instructions") },
                modifier = Modifier.fillMaxWidth().testTag("checkout_instructions_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Custom Delivery Location & Contact Details
            Text(
                text = viewModel.t("3. Custom Location & Contact (Optional)", "3. कस्टम स्थान और संपर्क (वैकल्पिक)"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = viewModel.t(
                            "If you want delivery to a different location or require a custom contact number for this order, specify them below.",
                            "यदि आप किसी अन्य स्थान पर डिलीवरी चाहते हैं या इस ऑर्डर के लिए कस्टम संपर्क नंबर की आवश्यकता है, तो नीचे विवरण दें।"
                        ),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = customLocation,
                        onValueChange = { customLocation = it },
                        label = { Text(viewModel.t("Custom Location / Landmarks", "कस्टम स्थान / लैंडमार्क")) },
                        placeholder = { Text("e.g. Near Shiv Temple, Flat 2B") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_delivery_location_input"),
                        singleLine = false,
                        maxLines = 2,
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    OutlinedTextField(
                        value = customPhone,
                        onValueChange = { customPhone = it },
                        label = { Text(viewModel.t("Contact Phone Number", "संपर्क फोन नंबर")) },
                        placeholder = { Text("e.g. +91 9876543210") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_delivery_phone_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secure Payment details choice
            Text(viewModel.t("4. Payment Option", "4. भुगतान विकल्प"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = true, onClick = {})
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(viewModel.t("Cash on Delivery (COD) / Pay on Pickup", "कैश ऑन डिलीवरी (सीओडी) / स्टोर भुगतान"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(
                        viewModel.t("Pay securely using cash, UPI, or Cards once your products are delivered.", "डिलीवरी होने पर नकद, यूपीआई या कार्ड से भुगतान करें।"),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Apply Coupon Code Section
            Text(viewModel.t("5. Apply Promo Coupon", "5. प्रोमो कूपन लागू करें"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var tempCouponCode by remember { mutableStateOf(viewModel.appliedCouponCode.value ?: "") }
                    OutlinedTextField(
                        value = tempCouponCode,
                        onValueChange = { tempCouponCode = it },
                        placeholder = { Text("e.g. WELCOME50, PANKAJ20") },
                        modifier = Modifier.weight(1f).testTag("coupon_code_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val coupon = viewModel.couponsList.find { it.code.equals(tempCouponCode.trim(), ignoreCase = true) }
                            if (coupon != null) {
                                if (summary.subtotal >= coupon.minOrderAmount) {
                                    viewModel.appliedCouponCode.value = coupon.code
                                    Toast.makeText(context, "Promo Applied! Saved ${coupon.discountPercent}%", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Requires Minimum Order of ₹${coupon.minOrderAmount.toInt()}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                if (tempCouponCode.isBlank()) {
                                    viewModel.appliedCouponCode.value = null
                                    Toast.makeText(context, "Coupon Cleared", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Invalid Coupon Code!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.height(50.dp).testTag("coupon_apply_btn")
                    ) {
                        Text(if (viewModel.appliedCouponCode.value != null) "Applied" else "Apply")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Invoice Checkout summary lines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Amount Payable", fontSize = 13.sp, color = Color.Gray)
                    Text("₹${summary.totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = {
                        if (viewModel.blockedCustomerIds.contains(1)) {
                            Toast.makeText(context, "Your account has been temporarily suspended by Pankaj Kirana Admin.", Toast.LENGTH_LONG).show()
                        } else {
                            val selectedAddr = addresses.find { it.id == viewModel.currentAddressSelectionId.value }
                            val baseAddr = selectedAddr?.addressLine ?: "Store Pickup - Sipara Main Road, Patna - 800020"
                            val finalAddr = buildString {
                                append(baseAddr)
                                if (customLocation.isNotBlank()) {
                                    append(" (Custom Location: ").append(customLocation.trim()).append(")")
                                }
                                if (customPhone.isNotBlank()) {
                                    append(" [Contact Phone: ").append(customPhone.trim()).append("]")
                                }
                            }
                            viewModel.placeOrder(summary, finalAddr, instructionsInput)
                            Toast.makeText(context, "Order Placed Successfully! 🎉", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("checkout_place_order_btn")
                ) {
                    Text("Confirm & Place Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// --- ORDER TRACKING VIEW ---
@Composable
fun OrderTrackingView(
    viewModel: GroceryViewModel,
    orderId: Int,
    ordersList: List<OrderEntity>
) {
    val order = ordersList.find { it.id == orderId } ?: return
    val products = viewModel.getOrderProducts(order)

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val context = LocalContext.current

    val showSuccessAlert = viewModel.showOrderPlacedSuccessAlert.value
    if (showSuccessAlert) {
        var inAppAlert by remember { mutableStateOf(true) }
        var soundVibeAlert by remember { mutableStateOf(true) }
        var sysNotifAlert by remember { mutableStateOf(true) }
        var whatsappAlertSim by remember { mutableStateOf(false) }
        var smsAlertSim by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.showOrderPlacedSuccessAlert.value = false },
            icon = {
                Text(text = "🎉", fontSize = 48.sp)
            },
            title = {
                Text(
                    text = viewModel.t("Order Placed Successfully! 🎉", "ऑर्डर सफलतापूर्वक भेजा गया! 🎉"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = viewModel.t(
                            "Thank you for ordering with Pankaj Kirana! Your Order ID is #${order.id}. Total amount: ₹${order.totalAmount.toInt()}.",
                            "पंकज किराना के साथ ऑर्डर करने के लिए धन्यवाद! आपकी ऑर्डर आईडी #${order.id} है। कुल राशि: ₹${order.totalAmount.toInt()}।"
                        ),
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text(
                        text = viewModel.t("🔔 Enable Live Status Alert Options:", "🔔 लाइव स्टेटस अलर्ट विकल्प चालू करें:"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // In-app alert option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(viewModel.t("In-App Status Overlay", "इन-ऐप स्टेटस ओवरले"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(viewModel.t("Show live popups while using app", "ऐप का उपयोग करते समय लाइव पॉपअप दिखाएं"), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = inAppAlert,
                            onCheckedChange = { inAppAlert = it },
                            modifier = Modifier.testTag("success_alert_inapp_switch")
                        )
                    }

                    // Sound & vibration alert option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(viewModel.t("Chime & Vibration Alert", "ध्वनि और कंपन अलर्ट"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(viewModel.t("Vibrate and play sound on status update", "ऑर्डर स्टेटस अपडेट होने पर ध्वनि व कंपन"), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = soundVibeAlert,
                            onCheckedChange = { soundVibeAlert = it },
                            modifier = Modifier.testTag("success_alert_soundvibe_switch")
                        )
                    }

                    // System notifications option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(viewModel.t("Phone System Notifications", "फोन सिस्टम नोटिफिकेशन"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(viewModel.t("Receive notifications in phone system tray", "फ़ोन सिस्टम ट्रे में सूचनाएं प्राप्त करें"), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = sysNotifAlert,
                            onCheckedChange = { sysNotifAlert = it },
                            modifier = Modifier.testTag("success_alert_sysnotif_switch")
                        )
                    }

                    // WhatsApp simulation option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Forum, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(viewModel.t("Send WhatsApp Live Updates", "व्हाट्सएप लाइव अपडेट भेजें"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(viewModel.t("Get order tickets instantly on WhatsApp", "व्हाट्सएप पर तुरंत ऑर्डर टिकट प्राप्त करें"), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Checkbox(
                            checked = whatsappAlertSim,
                            onCheckedChange = { whatsappAlertSim = it ?: false },
                            modifier = Modifier.testTag("success_alert_whatsapp_checkbox")
                        )
                    }

                    // SMS simulation option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(viewModel.t("Send SMS / Text Alerts", "एसएमएस / टेक्स्ट अलर्ट भेजें"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(viewModel.t("Regular carrier updates about delivery", "डिलीवरी के बारे में नियमित ऑपरेटर अपडेट"), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Checkbox(
                            checked = smsAlertSim,
                            onCheckedChange = { smsAlertSim = it ?: false },
                            modifier = Modifier.testTag("success_alert_sms_checkbox")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.orderAlertPrefsMap[order.id] = com.example.ui.OrderAlertPrefs(
                            orderId = order.id,
                            inAppStatusChangeAlert = inAppAlert,
                            soundAndVibrationAlert = soundVibeAlert,
                            systemNotificationAlert = sysNotifAlert,
                            whatsappAlertSim = whatsappAlertSim,
                            smsAlertSim = smsAlertSim
                        )
                        viewModel.showOrderPlacedSuccessAlert.value = false
                        Toast.makeText(context, viewModel.t("Alert options configured for this order!", "इस ऑर्डर के लिए अलर्ट विकल्प कॉन्फ़िगर किए गए!"), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("alert_preferences_save_btn")
                ) {
                    Text(viewModel.t("Save Preferences & Track Live", "प्राथमिकताएं सहेजें और लाइव ट्रैक करें"))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Order Tracker #${order.id}") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = "HOME" }, modifier = Modifier.testTag("tracker_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // General info header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pankaj Kirana Delivery Ticket", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Placed: ${dateFormat.format(Date(order.timestamp))}", fontSize = 12.sp, color = Color.Gray)
                    Text("Method: ${order.deliveryTime}", fontSize = 12.sp, color = Color.Gray)
                    Text("Delivery To: ${order.deliveryAddress}", fontSize = 12.sp, color = Color.Gray)
                    if (order.deliveryInstructions.isNotBlank()) {
                        Text("Instructions: ${order.deliveryInstructions}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alert preferences summary card with option to modify
            val currentPrefs = viewModel.orderAlertPrefsMap[order.id] ?: com.example.ui.OrderAlertPrefs(order.id)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔔 Active Alert Channels",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { viewModel.showOrderPlacedSuccessAlert.value = true },
                            modifier = Modifier.height(32.dp).testTag("modify_alerts_btn")
                        ) {
                            Text("Configure Options", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeChannels = mutableListOf<String>()
                        if (currentPrefs.inAppStatusChangeAlert) activeChannels.add("In-App 📱")
                        if (currentPrefs.soundAndVibrationAlert) activeChannels.add("Sound/Vibe 🔊")
                        if (currentPrefs.systemNotificationAlert) activeChannels.add("System 📲")
                        if (currentPrefs.whatsappAlertSim) activeChannels.add("WhatsApp 💬")
                        if (currentPrefs.smsAlertSim) activeChannels.add("SMS ✉️")

                        if (activeChannels.isEmpty()) {
                            Text("No alert channels selected.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            activeChannels.forEach { channel ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(channel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Tracking Visual Timeline ---
            Text("Order Progress Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            val stages = listOf("Order Confirmed", "Packing", "Out for Delivery", "Delivered")
            val currentStageIndex = stages.indexOf(order.status).coerceAtLeast(0)

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                stages.forEachIndexed { index, stageName ->
                    val isCompleted = index <= currentStageIndex
                    val isCurrent = index == currentStageIndex

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isCompleted) Color.White else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (index < stages.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(40.dp)
                                        .background(if (index < currentStageIndex) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stageName,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontSize = 14.sp
                            )
                            if (isCurrent) {
                                Text(
                                    text = "Estimated: ${order.estimatedDelivery}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Ordered Products Summary Breakdown
            Text("Items Ordered Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            products.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.productName} (x${item.quantity})", fontSize = 13.sp)
                    Text("₹${(item.priceAtOrder * item.quantity).toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Paid (COD)", fontWeight = FontWeight.Bold)
                Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Simulation trigger to test advancing order status!
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Customer Testing Panel 🧪", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Since this is a preview emulator, you can advance the order status locally to test the tracker stepper!", fontSize = 11.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val nextStage = when (order.status) {
                                "Order Confirmed" -> "Packing"
                                "Packing" -> "Out for Delivery"
                                "Out for Delivery" -> "Delivered"
                                else -> "Order Confirmed"
                            }
                            viewModel.adminUpdateOrderStatus(order.id, nextStage)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("simulate_status_advance_btn")
                    ) {
                        Text("Simulate Next Delivery Stage 🚀")
                    }
                }
            }
        }
    }
}

// --- WISHLIST VIEW ---
@Composable
fun WishlistView(
    viewModel: GroceryViewModel,
    productsList: List<GroceryProduct>,
    wishlist: List<WishlistItem>
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(viewModel.t("Your Wishlist", "आपकी पसंदीदा सूची")) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = "HOME" }, modifier = Modifier.testTag("wish_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (wishlist.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No items in Wishlist yet!", fontWeight = FontWeight.Bold)
                    Text("Tap heart on any item to save it here.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wishlist) { item ->
                    val prod = productsList.find { it.id == item.productId }
                    if (prod != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wishlist_row_${prod.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Price: ₹${prod.price.toInt()} | ${prod.weight}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Row {
                                    Button(
                                        onClick = {
                                            viewModel.addToCart(prod.id)
                                            Toast.makeText(context, "${prod.name} added to cart!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(32.dp).testTag("wish_move_to_cart_${prod.id}")
                                    ) {
                                        Text("Move to Cart", fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { viewModel.toggleWishlist(prod.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- LOYALTY & REFERRAL PROGRAM VIEW ---


// --- CUSTOMER SUPPORT & AI CHAT ASSISTANT ---
@Composable
fun CustomerSupportView(
    viewModel: GroceryViewModel,
    profile: UserProfileEntity?
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: AI Chatbot, 1: FAQ & Store Info
    var chatMessageInput by remember { mutableStateOf("") }

    val chatHistory by viewModel.aiChatHistory.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Pankaj Support Center") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = "HOME" }, modifier = Modifier.testTag("support_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Support tabs selectors
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("AI Shopping Assistant") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                    modifier = Modifier.testTag("support_tab_ai")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("FAQs & Contact") },
                    icon = { Icon(Icons.Default.HelpCenter, contentDescription = null) },
                    modifier = Modifier.testTag("support_tab_faq")
                )
            }

            if (selectedTab == 0) {
                // --- TAB A: AI CHAT ASSISTANT (Powered by Gemini) ---
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat messages scrollable shelf
                    val chatScrollState = rememberScrollState()
                    LaunchedEffect(chatHistory.size) {
                        chatScrollState.animateScrollTo(chatScrollState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(chatScrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Greeting message
                        ChatBubble(
                            sender = "Pankaj Assistant",
                            message = "Namaste! 🙏 Welcome to Pankaj Kirana Support Chat. Ask me recipes, store opening times, or product prices! How can I help you cook today?",
                            isUser = false
                        )

                        chatHistory.forEach { (usrMsg, modelReply) ->
                            ChatBubble(sender = "You", message = usrMsg, isUser = true)
                            if (modelReply.isNotEmpty()) {
                                ChatBubble(sender = "Pankaj Assistant", message = modelReply, isUser = false)
                            }
                        }

                        if (viewModel.isAiLoading.value) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    // Helpful Quick Prompt Pills
                    val prompts = listOf(
                        "Suggest healthy Indian breakfast recipes.",
                        "What is the contact number of Pankaj Kirana?",
                        "Do you sell Aashirvaad Atta?"
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(prompts) { p ->
                            AssistChip(
                                onClick = { viewModel.sendAiMessage(p) },
                                label = { Text(p, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.testTag("prompt_pill_${p.take(10)}")
                            )
                        }
                    }

                    // Message typing bottom bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessageInput,
                            onValueChange = { chatMessageInput = it },
                            placeholder = { Text("Ask anything...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatMessageInput.isNotBlank()) {
                                    viewModel.sendAiMessage(chatMessageInput)
                                    chatMessageInput = ""
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(48.dp)
                                .testTag("chat_send_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            } else {
                // --- TAB B: FAQS & CONTACT CHANNELS ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("Store Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Store Name: Pankaj Kirana", fontWeight = FontWeight.Bold)
                            Text("Location: Sipara Road, near Shiv Mandir, Sipara, Patna, Bihar - 800020")
                            Text("Contact Helpline: 8235091376", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text("Email Helpdesk: support@pankajkirana.com")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Contact Helplines", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { Toast.makeText(context, "Dialing 8235091376...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).testTag("phone_support_btn")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Phone Support")
                        }
                        Button(
                            onClick = { Toast.makeText(context, "Opening WhatsApp chat with Pankaj Kirana...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).testTag("whatsapp_support_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp Support", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Frequently Asked Questions (FAQs)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val faqs = listOf(
                        "What are the store delivery timings?" to "We deliver daily between 8:00 AM and 8:00 PM. Orders placed after 7:30 PM are scheduled for next-day delivery.",
                        "Is there any minimum order limit for free delivery?" to "Yes, free home delivery is automatically applied to orders valued above ₹500. Otherwise, a nominal delivery fee of ₹30 is charged.",
                        "What is your return and replacement policy?" to "If you receive damaged, expired, or incorrect products, you can request a refund/replacement via Order History within 24 hours of delivery. No questions asked!"
                    )

                    faqs.forEach { (q, a) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(q, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(a, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    sender: String,
    message: String,
    isUser: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Text(sender, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 12.dp
                    )
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}
