package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GroceryViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[GroceryViewModel::class.java]

        setContent {
            val isDark = viewModel.isDarkTheme.value
            MyApplicationTheme(darkTheme = isDark) {
                MainContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(viewModel: GroceryViewModel) {
    val context = LocalContext.current

    // Observe Room flows safely
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlist by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val addresses by viewModel.allAddresses.collectAsStateWithLifecycle()

    val screen = viewModel.currentScreen.value
    val isUserLoggedIn = viewModel.isLoggedIn.value

    if ((screen == "SPLASH") || (!isUserLoggedIn)) {
        // --- AUTH & INTRO SCREEN ---
        AuthAndIntroScreen(viewModel = viewModel)
    } else {
        // --- CORE APPLICATION WITH BOTTOM NAVIGATION BAR ---
        val cartActiveCount = remember(cart) { cart.filter { !it.isSavedForLater }.sumOf { it.quantity } }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = viewModel.t("LOCAL GROCERY STORE", "स्थानीय किराना दुकान"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pankaj Kirana",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Hindi / English Toggle
                        IconButton(
                            onClick = {
                                viewModel.currentLanguage.value = if (viewModel.currentLanguage.value == "EN") "HI" else "EN"
                                Toast.makeText(context, "Language changed to ${viewModel.currentLanguage.value}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("lang_toggle_btn")
                        ) {
                            Text(
                                text = if (viewModel.currentLanguage.value == "EN") "हिं" else "EN",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        // Light / Dark Theme Toggle
                        IconButton(
                            onClick = { viewModel.isDarkTheme.value = !viewModel.isDarkTheme.value },
                            modifier = Modifier.testTag("theme_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (viewModel.isDarkTheme.value) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }

                        // Quick Log out button
                        IconButton(
                            onClick = {
                                viewModel.isLoggedIn.value = false
                                viewModel.currentScreen.value = "SPLASH"
                                Toast.makeText(context, "Logged out of store", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("logout_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = screen == "HOME" || screen == "PRODUCT_DETAILS",
                        onClick = { viewModel.currentScreen.value = "HOME" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(viewModel.t("Shop", "दुकान")) },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = screen == "CART" || screen == "CHECKOUT",
                        onClick = { viewModel.currentScreen.value = "CART" },
                        icon = {
                            BadgedBox(badge = {
                                if (cartActiveCount > 0) {
                                    Badge { Text(cartActiveCount.toString()) }
                                }
                            }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        },
                        label = { Text(viewModel.t("Cart", "कार्ट")) },
                        modifier = Modifier.testTag("nav_cart")
                    )
                    NavigationBarItem(
                        selected = screen == "WISHLIST",
                        onClick = { viewModel.currentScreen.value = "WISHLIST" },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Wishlist") },
                        label = { Text(viewModel.t("Favs", "पसंदीदा")) },
                        modifier = Modifier.testTag("nav_wishlist")
                    )
                    NavigationBarItem(
                        selected = screen == "SUPPORT",
                        onClick = { viewModel.currentScreen.value = "SUPPORT" },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Support") },
                        label = { Text(viewModel.t("AI Help", "सहायता")) },
                        modifier = Modifier.testTag("nav_support")
                    )

                    // Dynamic ADMIN PANEL desk navigation item
                    NavigationBarItem(
                        selected = screen.startsWith("ADMIN_") || screen == "ADMIN_DASHBOARD",
                        onClick = { viewModel.currentScreen.value = "ADMIN_DASHBOARD" },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                        label = { Text("Admin") },
                        modifier = Modifier.testTag("nav_admin")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (screen) {
                    "HOME" -> CustomerHomeScreen(
                        viewModel = viewModel,
                        products = products
                    )
                    "PRODUCT_DETAILS" -> {
                        val prodId = viewModel.selectedProductId.value ?: 1
                        ProductDetailsView(viewModel = viewModel, productId = prodId, products = products)
                    }
                    "CART" -> ShoppingCartView(viewModel = viewModel, productsList = products, cartItems = cart)
                    "CHECKOUT" -> CheckoutView(viewModel = viewModel, productsList = products, cartItems = cart, addresses = addresses)
                    "ORDER_TRACKING" -> {
                        val ordId = viewModel.activeOrderId.value ?: 1
                        OrderTrackingView(viewModel = viewModel, orderId = ordId, ordersList = orders)
                    }
                    "WISHLIST" -> WishlistView(viewModel = viewModel, productsList = products, wishlist = wishlist)

                    "SUPPORT" -> CustomerSupportView(viewModel = viewModel)
                    
                    // Admin Screens Mapping
                    "ADMIN_DASHBOARD" -> AdminDashboardScreen(
                        viewModel = viewModel,
                        orders = orders,
                        products = products
                    )
                    else -> CustomerHomeScreen(viewModel = viewModel, products = products)
                }
            }
        }
    }
}

@Composable
fun AuthAndIntroScreen(viewModel: GroceryViewModel) {
    val context = LocalContext.current
    var inputMobile by remember { mutableStateOf("") }
    var inputOtp by remember { mutableStateOf("") }
    var showOtpBlock by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Large Custom Adaptive App Icon that we generated!
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "Pankaj Kirana Logo",
                modifier = Modifier.size(90.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pankaj Kirana",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "पंकज किराना - रांची, झारखंड",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Fresh Grains, Ghee, Cooking Oils, Spices and daily household staples delivered to your home instantly.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // OTP Login Block Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (showOtpBlock) "Verify OTP Code" else "Mobile OTP Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (!showOtpBlock) {
                    OutlinedTextField(
                        value = inputMobile,
                        onValueChange = { inputMobile = it },
                        label = { Text("10-Digit Mobile Number") },
                        placeholder = { Text("e.g. 8235091376") },
                        modifier = Modifier.fillMaxWidth().testTag("auth_mobile_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (inputMobile.length == 10) {
                                showOtpBlock = true
                                Toast.makeText(context, "OTP Code '123456' sent to $inputMobile", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Please enter a valid 10-digit number", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("send_otp_btn")
                    ) {
                        Text("Send OTP Verification Code")
                    }
                } else {
                    Text("Enter the 6-digit OTP code sent to your mobile.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputOtp,
                        onValueChange = { inputOtp = it },
                        label = { Text("6-Digit OTP Code") },
                        placeholder = { Text("e.g. 123456") },
                        modifier = Modifier.fillMaxWidth().testTag("auth_otp_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (inputOtp == "123456" || inputOtp.length == 6) {
                                viewModel.isLoggedIn.value = true
                                viewModel.currentScreen.value = "HOME"
                                Toast.makeText(context, "OTP Verified! Welcome to Pankaj Kirana.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid OTP! Hint: Enter 123456", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("verify_otp_submit_btn")
                    ) {
                        Text("Verify & Enter Store")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = { showOtpBlock = false }) {
                        Text("Back to Mobile input")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Quick Bypass Button
        Button(
            onClick = {
                viewModel.isLoggedIn.value = true
                viewModel.currentScreen.value = "HOME"
                Toast.makeText(context, "Entered as Guest", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("guest_bypass_btn")
        ) {
            Text("Quick Guest Access / Bypass Sign In 🚀", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Store Opening stats footer
        Text("Store Address: Market Yard, Ranchi, Jharkhand - 834001", fontSize = 11.sp, color = Color.Gray)
        Text("Helpline Phone Support: 8235091376", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}
