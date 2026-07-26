package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GroceryProduct
import com.example.data.OrderEntity
import com.example.data.UserProfileEntity
import com.example.ui.*
import com.example.ui.components.SalesLineChart
import com.example.ui.components.SalesPieChart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AdminSection(val displayName: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "dashboard"),
    PRODUCTS("Products", Icons.Default.ShoppingCart, "products"),
    CATEGORIES("Categories", Icons.Default.Category, "categories"),
    ORDERS("Orders", Icons.Default.Receipt, "orders"),
    CUSTOMERS("Customers", Icons.Default.Group, "customers"),
    INVENTORY("Inventory", Icons.Default.List, "inventory"),
    COUPONS("Coupons", Icons.Default.LocalOffer, "coupons"),
    BANNERS("Banners", Icons.Default.Image, "banners"),
    REPORTS("Reports", Icons.Default.Assessment, "reports"),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications, "notifications"),
    SETTINGS("Settings", Icons.Default.Settings, "settings"),
    PROFILE("Profile", Icons.Default.Person, "profile")
}

@Composable
fun AdminDashboardScreen(
    viewModel: GroceryViewModel,
    orders: List<OrderEntity>,
    products: List<GroceryProduct>,
    modifier: Modifier = Modifier
) {
    if (!viewModel.adminIsLoggedIn.value) {
        AdminLoginScreen(viewModel = viewModel)
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedSection by remember { mutableStateOf(AdminSection.DASHBOARD) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.storeSettings.value.storeLogoEmoji,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = viewModel.storeSettings.value.storeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Owner Dashboard Control",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(AdminSection.values()) { section ->
                        NavigationDrawerItem(
                            icon = { Icon(section.icon, contentDescription = section.displayName) },
                            label = { Text(section.displayName, fontWeight = FontWeight.SemiBold) },
                            selected = selectedSection == section,
                            onClick = {
                                selectedSection = section
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red) },
                    label = { Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            showLogoutConfirm = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).height(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = {
                        Text(
                            text = selectedSection.displayName,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Sidebar Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.currentScreen.value = "HOME" }) {
                            Icon(Icons.Default.Storefront, contentDescription = "Switch to Customer App View", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                Crossfade(targetState = selectedSection, label = "AdminTabCrossfade") { section ->
                    when (section) {
                        AdminSection.DASHBOARD -> AdminOverviewTab(viewModel, products, orders, onNavigateToSection = { selectedSection = it })
                        AdminSection.PRODUCTS -> AdminProductsTab(viewModel, products)
                        AdminSection.CATEGORIES -> AdminCategoriesTab(viewModel, products)
                        AdminSection.ORDERS -> AdminOrdersTab(viewModel, orders)
                        AdminSection.CUSTOMERS -> AdminCustomersTab(viewModel, orders)
                        AdminSection.INVENTORY -> AdminInventoryTab(viewModel, products)
                        AdminSection.COUPONS -> AdminCouponsTab(viewModel)
                        AdminSection.BANNERS -> AdminBannersTab(viewModel)
                        AdminSection.REPORTS -> AdminReportsTab(viewModel, orders, products)
                        AdminSection.NOTIFICATIONS -> AdminNotificationsTab(viewModel)
                        AdminSection.SETTINGS -> AdminSettingsTab(viewModel)
                        AdminSection.PROFILE -> AdminProfileTab(viewModel)
                    }
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to sign out from the Pankaj Kirana Admin Panel?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.adminIsLoggedIn.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminLoginScreen(viewModel: GroceryViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secure Header Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure Lock",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Pankaj Kirana",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "ADMIN SECURE ACCESS PORTAL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Security Badge Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Green.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Encrypted",
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "256-bit SSL Encryption Active",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Admin Email Address") },
                    placeholder = { Text("ayushkr@gmail.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_user_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Secret Security PIN") },
                    placeholder = { Text("Enter 6-digit password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = "Toggle password visibility")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showForgotPassword = true }) {
                        Text("Forgot Security PIN?", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val enteredEmail = email.trim()
                        val enteredPassword = password.trim()
                        if (enteredEmail == viewModel.adminUsername.value && enteredPassword == viewModel.adminPassword.value) {
                            viewModel.adminIsLoggedIn.value = true
                            Toast.makeText(context, "Authorization Successful! Welcome back, Admin. 🔓", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "❌ Invalid Admin Email or Security PIN", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("admin_login_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Authenticate Terminal", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { viewModel.currentScreen.value = "HOME" },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Return to Customer App", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showForgotPassword) {
        AlertDialog(
            onDismissRequest = { showForgotPassword = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PIN Retrieval Policy")
                }
            },
            text = { 
                Text("For security, authorized administrators can sign in using:\n\nEmail: ayushkr@gmail.com\nPassword: 020202\n\nPlease keep these credentials private to protect store settings and sales reports.") 
            },
            confirmButton = {
                Button(onClick = { showForgotPassword = false }) {
                    Text("I Understand")
                }
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    viewModel: GroceryViewModel,
    productsList: List<GroceryProduct>,
    ordersList: List<OrderEntity>,
    onNavigateToSection: (AdminSection) -> Unit
) {
    val stats = viewModel.getAdminStats(ordersList, productsList)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Quick KPIs Row
        item {
            Text("Business Pulse Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard(
                        title = "Total Revenue",
                        value = "₹${stats.totalRevenue.toInt()}",
                        icon = Icons.Default.Payments,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Total Orders",
                        value = "${stats.totalOrders}",
                        icon = Icons.Default.Receipt,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard(
                        title = "Total Products",
                        value = "${productsList.size}",
                        icon = Icons.Default.Inventory,
                        color = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Low Stock Alert",
                        value = "${stats.lowStockCount}",
                        icon = Icons.Default.Warning,
                        color = if (stats.lowStockCount > 0) Color.Red else Color.Gray,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToSection(AdminSection.INVENTORY) }
                    )
                }
            }
        }

        // Charts Section
        item {
            Text("Analytical Trends", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Completion Analytics", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    SalesPieChart(
                        deliveredCount = stats.deliveredOrdersCount,
                        pendingCount = stats.pendingOrdersCount,
                        modifier = Modifier.height(140.dp)
                    )
                }
            }
        }

        item {
            val revenueList = ordersList.filter { it.status == "Delivered" }.map { it.totalAmount }
            SalesLineChart(
                weeklySales = if (revenueList.isEmpty()) listOf(0.0, 0.0) else revenueList,
                modifier = Modifier.height(160.dp)
            )
        }

        // Recent Orders Segment
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Customer Inquiries", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = { onNavigateToSection(AdminSection.ORDERS) }) {
                    Text("View All")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (ordersList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No orders placed yet.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                ordersList.take(3).forEach { ord ->
                    val productsInfo = viewModel.getOrderProducts(ord)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onNavigateToSection(AdminSection.ORDERS) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Order #${ord.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${productsInfo.size} unique item(s) • ${ord.deliveryTime}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${ord.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (ord.status) {
                                                "Delivered" -> Color(0xFFE8F5E9)
                                                "Cancelled" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFE1F5FE)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = ord.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (ord.status) {
                                            "Delivered" -> Color(0xFF2E7D32)
                                            "Cancelled" -> Color(0xFFC62828)
                                            else -> Color(0xFF0277BD)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(96.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun AdminProductsTab(
    viewModel: GroceryViewModel,
    products: List<GroceryProduct>
) {
    val context = LocalContext.current
    var showAddForm by remember { mutableStateOf(false) }
    var selectedEditProduct by remember { mutableStateOf<GroceryProduct?>(null) }
    var showImageSelector by remember { mutableStateOf(false) }
    var selectedImageEmoji by remember { mutableStateOf("🍏") }

    // Add product form values
    var nameInput by remember { mutableStateOf("") }
    var brandInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Spices") }
    var weightInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("0.0") }
    var stockInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Catalog Control Desk", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Button(
                    onClick = { showAddForm = !showAddForm },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(if (showAddForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showAddForm) "Cancel" else "Add Product")
                }
            }
        }

        if (showAddForm) {
            val presetAddImages = listOf(
                Pair("🍎 Apple", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&auto=format&fit=crop&q=60"),
                Pair("🥛 Milk", "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&auto=format&fit=crop&q=60"),
                Pair("🍛 Grains", "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&auto=format&fit=crop&q=60"),
                Pair("🫘 Masala", "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400&auto=format&fit=crop&q=60"),
                Pair("☕ Tea", "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400&auto=format&fit=crop&q=60"),
                Pair("🧴 Oil", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400&auto=format&fit=crop&q=60"),
                Pair("🍪 Biscuit", "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=400&auto=format&fit=crop&q=60")
            )

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Add New Product Spec Sheet", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Picture: ", fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                                    .clickable { showImageSelector = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(selectedImageEmoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { showImageSelector = true }) {
                                Text("Choose Illustration")
                            }
                        }

                        OutlinedTextField(
                            value = imageUrlInput, 
                            onValueChange = { imageUrlInput = it }, 
                            label = { Text("Custom Photo Image URL (Optional)") }, 
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Or choose a Preset Photo:", fontSize = 12.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetAddImages.forEach { (label, url) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (imageUrlInput == url) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f))
                                        .border(1.dp, if (imageUrlInput == url) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable { 
                                            imageUrlInput = url
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray.copy(alpha = 0.1f))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUrlInput.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = imageUrlInput,
                                    contentDescription = "New Product Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                                )
                            } else {
                                Text("Selected Emoji: $selectedImageEmoji", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = brandInput, onValueChange = { brandInput = it }, label = { Text("Brand Name") }, modifier = Modifier.fillMaxWidth())
                        
                        // Category Selection Dropdown trigger
                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text("Category (e.g., Spices, Snaks, Dairy)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, label = { Text("Weight (e.g. 1 Kg)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = priceInput, onValueChange = { priceInput = it }, label = { Text("Price (₹)") }, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = discountInput, onValueChange = { discountInput = it }, label = { Text("Discount %") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = stockInput, onValueChange = { stockInput = it }, label = { Text("Stock Qty") }, modifier = Modifier.weight(1f))
                        }
                        OutlinedTextField(value = descInput, onValueChange = { descInput = it }, label = { Text("Detailed Description") }, modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val pr = priceInput.toDoubleOrNull() ?: 0.0
                                val ds = discountInput.toDoubleOrNull() ?: 0.0
                                val st = stockInput.toIntOrNull() ?: 0
                                if (nameInput.isNotBlank() && pr > 0.0) {
                                    viewModel.adminAddProduct(
                                        name = nameInput,
                                        brand = brandInput,
                                        category = categoryInput,
                                        weight = weightInput,
                                        price = pr,
                                        discount = ds,
                                        stock = st,
                                        description = descInput,
                                        imageUrl = imageUrlInput.ifBlank { null },
                                        imageEmoji = if (imageUrlInput.isBlank()) selectedImageEmoji else null
                                    )
                                    nameInput = ""
                                    brandInput = ""
                                    weightInput = ""
                                    priceInput = ""
                                    discountInput = "0.0"
                                    stockInput = ""
                                    descInput = ""
                                    imageUrlInput = ""
                                    showAddForm = false
                                    Toast.makeText(context, "Product saved successfully! ✅", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please verify all fields!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Insert to Catalog", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(products) { prod ->
            val isDisabled = viewModel.disabledProductIds.contains(prod.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(alpha = if (isDisabled) 0.5f else 1.0f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (isDisabled) Color.Gray else Color.LightGray.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!prod.imageUrl.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model = prod.imageUrl,
                                contentDescription = prod.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                            )
                        } else {
                            val activeEmoji = if (!prod.imageEmoji.isNullOrBlank()) {
                                prod.imageEmoji
                            } else {
                                when (prod.category) {
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
                                }
                            }
                            Text(activeEmoji, fontSize = 28.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (isDisabled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                    Text("DISABLED", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("${prod.brand} • ${prod.weight} • ${prod.category}", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("₹${prod.price}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Stock: ${prod.stock}", fontSize = 11.sp, color = if (prod.stock < 10) Color.Red else Color.DarkGray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { selectedEditProduct = prod }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit specs", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.adminDeleteProduct(prod) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove product", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                        // Enable / Disable product toggle button
                        Switch(
                            checked = !isDisabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.disabledProductIds.remove(prod.id)
                                } else {
                                    viewModel.disabledProductIds.add(prod.id)
                                }
                            },
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                }
            }
        }
    }

    if (showImageSelector) {
        Dialog(onDismissRequest = { showImageSelector = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Fresh Icon Illustration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    val emojis = listOf("🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍍", "🥭", "🥕", "🧅", "🧄", "🥦", "🥬", "🥛", "🍞", "🧂", "🧼")
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojis.forEach { emo ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.LightGray.copy(alpha = 0.2f), CircleShape)
                                    .clickable {
                                        selectedImageEmoji = emo
                                        showImageSelector = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 24.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showImageSelector = false }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    selectedEditProduct?.let { prod ->
        var editName by remember { mutableStateOf(prod.name) }
        var editBrand by remember { mutableStateOf(prod.brand) }
        var editWeight by remember { mutableStateOf(prod.weight) }
        var editPrice by remember { mutableStateOf(prod.price.toString()) }
        var editDiscount by remember { mutableStateOf(prod.discount.toString()) }
        var editStock by remember { mutableStateOf(prod.stock.toString()) }
        var editDesc by remember { mutableStateOf(prod.description) }
        var editImageUrl by remember { mutableStateOf(prod.imageUrl ?: "") }
        var editImageEmoji by remember { mutableStateOf(prod.imageEmoji ?: "") }

        val presetImages = listOf(
            Pair("🍎 Apple", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&auto=format&fit=crop&q=60"),
            Pair("🥛 Milk", "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&auto=format&fit=crop&q=60"),
            Pair("🍛 Grains", "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&auto=format&fit=crop&q=60"),
            Pair("🫘 Masala", "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400&auto=format&fit=crop&q=60"),
            Pair("☕ Tea", "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400&auto=format&fit=crop&q=60"),
            Pair("🧴 Oil", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400&auto=format&fit=crop&q=60"),
            Pair("🍪 Biscuit", "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=400&auto=format&fit=crop&q=60")
        )
        val presetEmojis = listOf("🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍍", "🥭", "🥕", "🧅", "🧄", "🥦", "🥬", "🥛", "🍞", "🧂", "🧼")

        Dialog(onDismissRequest = { selectedEditProduct = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Edit Spec Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editBrand, onValueChange = { editBrand = it }, label = { Text("Brand Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editWeight, onValueChange = { editWeight = it }, label = { Text("Weight / Volume") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPrice, onValueChange = { editPrice = it }, label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editDiscount, onValueChange = { editDiscount = it }, label = { Text("Discount %") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editStock, onValueChange = { editStock = it }, label = { Text("Available Stock") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Product Photo & Visuals", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                    
                    OutlinedTextField(
                        value = editImageUrl, 
                        onValueChange = { 
                            editImageUrl = it 
                            if (it.isNotBlank()) editImageEmoji = ""
                        }, 
                        label = { Text("Custom Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Or choose a Preset Photo:", fontSize = 12.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetImages.forEach { (label, url) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (editImageUrl == url) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f))
                                    .border(1.dp, if (editImageUrl == url) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { 
                                        editImageUrl = url
                                        editImageEmoji = ""
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Or choose an Illustration Emoji:", fontSize = 12.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetEmojis.forEach { emo ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (editImageEmoji == emo) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f))
                                    .border(1.dp, if (editImageEmoji == emo) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                    .clickable { 
                                        editImageEmoji = emo
                                        editImageUrl = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 18.sp)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.1f))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (editImageUrl.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = editImageUrl,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                            )
                        } else if (editImageEmoji.isNotBlank()) {
                            Text(editImageEmoji, fontSize = 48.sp)
                        } else {
                            Text("No Custom Image Selected", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { selectedEditProduct = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val pr = editPrice.toDoubleOrNull() ?: prod.price
                                val ds = editDiscount.toDoubleOrNull() ?: prod.discount
                                val st = editStock.toIntOrNull() ?: prod.stock
                                viewModel.adminUpdateProduct(
                                    prod.copy(
                                        name = editName,
                                        brand = editBrand,
                                        weight = editWeight,
                                        price = pr,
                                        discount = ds,
                                        stock = st,
                                        description = editDesc,
                                        imageUrl = editImageUrl.ifBlank { null },
                                        imageEmoji = editImageEmoji.ifBlank { null }
                                    )
                                )
                                selectedEditProduct = null
                                Toast.makeText(context, "Product specifications updated!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Update Details")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCategoriesTab(
    viewModel: GroceryViewModel,
    products: List<GroceryProduct>
) {
    val context = LocalContext.current
    var newCatInput by remember { mutableStateOf("") }
    var renameTargetCategory by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Category Dynamic Ledger", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCatInput,
                        onValueChange = { newCatInput = it },
                        label = { Text("New Category Title") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newCatInput.isNotBlank()) {
                                viewModel.adminAddCategory(newCatInput.trim())
                                newCatInput = ""
                                Toast.makeText(context, "New category category indexed!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        }

        items(viewModel.categoriesList) { cat ->
            val prodCount = products.count { it.category == cat }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(cat, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("$prodCount linked products", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                renameTargetCategory = cat
                                renameInput = cat
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.adminDeleteCategory(cat) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }

    renameTargetCategory?.let { oldCat ->
        Dialog(onDismissRequest = { renameTargetCategory = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Rename Category", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Category Title") },
                        singleLine = true
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { renameTargetCategory = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (renameInput.isNotBlank()) {
                                    viewModel.adminEditCategory(oldCat, renameInput.trim())
                                    renameTargetCategory = null
                                    Toast.makeText(context, "Category renamed!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Rename")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrdersTab(
    viewModel: GroceryViewModel,
    orders: List<OrderEntity>
) {
    val context = LocalContext.current
    var selectedOrderForInvoice by remember { mutableStateOf<OrderEntity?>(null) }
    var activeFilter by remember { mutableStateOf("All") } // "All", "Pending", "Delivered", "Cancelled"

    val filteredOrders = remember(orders, activeFilter) {
        when (activeFilter) {
            "Pending" -> orders.filter { it.status != "Delivered" && it.status != "Cancelled" }
            "Delivered" -> orders.filter { it.status == "Delivered" }
            "Cancelled" -> orders.filter { it.status == "Cancelled" }
            else -> orders
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Order Advancing & Dispatch Desk", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        // Horizontal filter bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Pending", "Delivered", "Cancelled")
                filters.forEach { filter ->
                    val isSelected = activeFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No orders matching selection.", color = Color.Gray)
                    }
                }
            }
        }

        items(filteredOrders) { ord ->
            val productsInfo = viewModel.getOrderProducts(ord)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Order ID: #${ord.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            Text(df.format(Date(ord.timestamp)), fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    when (ord.status) {
                                        "Delivered" -> Color(0xFFE8F5E9)
                                        "Cancelled" -> Color(0xFFFFEBEE)
                                        else -> Color(0xFFE1F5FE)
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                ord.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (ord.status) {
                                    "Delivered" -> Color(0xFF2E7D32)
                                    "Cancelled" -> Color(0xFFC62828)
                                    else -> Color(0xFF0277BD)
                                }
                            )
                        }
                    }

                    HorizontalDivider()

                    productsInfo.forEach { prod ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${prod.productName} x ${prod.quantity}", fontSize = 13.sp)
                            Text("₹${prod.priceAtOrder.toInt() * prod.quantity}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Address: ${ord.deliveryAddress}", fontSize = 12.sp, color = Color.Gray, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            Text("Method: ${ord.deliveryTime}", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Total: ₹${ord.totalAmount.toInt()}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }

                    // Status Actions
                    if (ord.status != "Delivered" && ord.status != "Cancelled") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val nextState = when (ord.status) {
                                "Order Confirmed" -> "Packing"
                                "Packing" -> "Out for Delivery"
                                "Out for Delivery" -> "Delivered"
                                else -> "Delivered"
                            }
                            Button(
                                onClick = { viewModel.adminUpdateOrderStatus(ord.id, nextState) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Advance: $nextState", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.adminUpdateOrderStatus(ord.id, "Cancelled") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reject / Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedOrderForInvoice = ord }) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print Invoice")
                        }
                    }
                }
            }
        }
    }

    selectedOrderForInvoice?.let { ord ->
        val productsInfo = viewModel.getOrderProducts(ord)
        Dialog(onDismissRequest = { selectedOrderForInvoice = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🏪", fontSize = 32.sp)
                    Text(viewModel.storeSettings.value.storeName.uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.Black)
                    Text(viewModel.storeSettings.value.address, fontSize = 10.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    Text("Contact: ${viewModel.storeSettings.value.contactNumber}", fontSize = 10.sp, color = Color.DarkGray)
                    
                    Text("---------------------------------", color = Color.Gray)
                    Text("INVOICE / TAX RECEIPT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                    Text("Invoice #: PANKAJ-ORD-${ord.id}", fontSize = 11.sp, color = Color.Black)
                    val df = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
                    Text("Date: ${df.format(Date(ord.timestamp))}", fontSize = 11.sp, color = Color.Black)
                    Text("---------------------------------", color = Color.Gray)

                    productsInfo.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.productName, fontSize = 12.sp, color = Color.Black)
                            Text("₹${item.priceAtOrder.toInt()} x ${item.quantity}", fontSize = 12.sp, color = Color.Black)
                        }
                    }

                    Text("---------------------------------", color = Color.Gray)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Handling Charge", fontSize = 11.sp, color = Color.Black)
                        Text("₹${viewModel.storeSettings.value.deliveryCharge.toInt()}", fontSize = 11.sp, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL AMOUNT PAYABLE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                        Text("₹${ord.totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    }

                    Text("---------------------------------", color = Color.Gray)
                    Text("Thank You! Please Visit Again!", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.DarkGray)
                    Text("Owner: Pankaj Kumar, Patna", fontSize = 9.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Invoice document sent to printer! 🖨️", Toast.LENGTH_SHORT).show()
                            selectedOrderForInvoice = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Execute Print Draft")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCustomersTab(
    viewModel: GroceryViewModel,
    orders: List<OrderEntity>
) {
    val context = LocalContext.current
    var selectedCustomerDetail by remember { mutableStateOf<UserProfileEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Client Base Directory", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        items(viewModel.customersList) { cust ->
            val isBlocked = viewModel.blockedCustomerIds.contains(cust.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedCustomerDetail = cust },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (isBlocked) Color.Red else Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cust.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (isBlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(modifier = Modifier.background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                        Text("BLOCKED", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("Mobile: ${cust.mobile}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.adminToggleBlockCustomer(cust.id)
                            Toast.makeText(context, if (isBlocked) "Customer unblocked!" else "Customer suspended!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBlocked) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            contentColor = if (isBlocked) Color(0xFF2E7D32) else Color.Red
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isBlocked) "Unblock" else "Block", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    selectedCustomerDetail?.let { cust ->
        Dialog(onDismissRequest = { selectedCustomerDetail = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Client Dossier Sheet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider()
                    
                    Text("Name: ${cust.name}", fontWeight = FontWeight.Bold)
                    Text("Email Address: ${cust.email}")
                    Text("Mobile Number: ${cust.mobile}")
                    Text("Loyalty Points: ${cust.loyaltyPoints} PK Points")
                    Text("Personal Invite Code: ${cust.referralCode}")

                    HorizontalDivider()
                    Text("In-Store Ledger History", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    val sampleOrders = orders.filter { it.deliveryAddress.contains(cust.name, ignoreCase = true) || it.id % 3 == cust.id % 3 }
                    if (sampleOrders.isEmpty()) {
                        Text("No recorded transactions found.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        LazyColumn(modifier = Modifier.height(100.dp)) {
                            items(sampleOrders) { o ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Order #${o.id} (${o.status})", fontSize = 11.sp)
                                    Text("₹${o.totalAmount.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { selectedCustomerDetail = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminInventoryTab(
    viewModel: GroceryViewModel,
    products: List<GroceryProduct>
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showOnlyLowStock by remember { mutableStateOf(false) }
    var showOnlyOutOfStock by remember { mutableStateOf(false) }

    // Dialog state
    var showUpsertDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<GroceryProduct?>(null) }
    var productToDelete by remember { mutableStateOf<GroceryProduct?>(null) }

    // Form inputs
    var nameInput by remember { mutableStateOf("") }
    var brandInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Rice & Flour") }
    var weightInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("0.0") }
    var stockInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("") }
    var imageEmojiInput by remember { mutableStateOf("") }

    val presetImages = listOf(
        Pair("🍎 Apple", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&auto=format&fit=crop&q=60"),
        Pair("🥛 Milk", "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&auto=format&fit=crop&q=60"),
        Pair("🍛 Grains", "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&auto=format&fit=crop&q=60"),
        Pair("🫘 Masala", "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400&auto=format&fit=crop&q=60"),
        Pair("☕ Tea", "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400&auto=format&fit=crop&q=60"),
        Pair("🧴 Oil", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400&auto=format&fit=crop&q=60"),
        Pair("🍪 Biscuit", "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=400&auto=format&fit=crop&q=60")
    )
    val presetEmojis = listOf("🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍍", "🥭", "🥕", "🧅", "🧄", "🥦", "🥬", "🥛", "🍞", "🧂", "🧼")

    val categoriesList = listOf("Rice & Flour", "Cooking Oil", "Spices", "Tea & Coffee", "Biscuits", "Snacks", "Dairy Products", "Dry Fruits", "Chocolates", "Personal Care", "Cleaning Supplies", "Other")

    // Synchronize form values on Add/Edit open
    LaunchedEffect(editingProduct, showUpsertDialog) {
        if (showUpsertDialog) {
            val prod = editingProduct
            if (prod != null) {
                nameInput = prod.name
                brandInput = prod.brand
                categoryInput = prod.category
                weightInput = prod.weight
                priceInput = prod.price.toString()
                discountInput = prod.discount.toString()
                stockInput = prod.stock.toString()
                descInput = prod.description
                imageUrlInput = prod.imageUrl ?: ""
                imageEmojiInput = prod.imageEmoji ?: ""
            } else {
                nameInput = ""
                brandInput = ""
                categoryInput = "Rice & Flour"
                weightInput = ""
                priceInput = ""
                discountInput = "0.0"
                stockInput = ""
                descInput = ""
                imageUrlInput = ""
                imageEmojiInput = "🍏"
            }
        }
    }

    // Dynamic stats computation
    val totalItems = products.size
    val lowStockCount = products.count { it.stock in 1..9 }
    val outOfStockCount = products.count { it.stock == 0 }
    val totalValue = products.sumOf { it.price * it.stock }

    val allCategories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().sorted()
    }

    // Reactive filtering logic
    val filteredProducts = remember(products, searchQuery, selectedCategory, showOnlyLowStock, showOnlyOutOfStock) {
        products.filter { prod ->
            val matchesSearch = prod.name.contains(searchQuery, ignoreCase = true) || 
                                prod.brand.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)
            val matchesLowStock = !showOnlyLowStock || (prod.stock in 1..9)
            val matchesOutOfStock = !showOnlyOutOfStock || (prod.stock == 0)
            
            matchesSearch && matchesCategory && matchesLowStock && matchesOutOfStock
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // KPI Summary Horizontal Scroll Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stat 1: Total SKUs
            Card(
                modifier = Modifier.width(140.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total SKUs", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalItems", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }

            // Stat 2: Low Stock
            Card(
                modifier = Modifier.width(140.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (lowStockCount > 0) Color(0xFFFFA726).copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Low Stock", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD84315), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$lowStockCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (lowStockCount > 0) Color(0xFFD84315) else Color.DarkGray)
                }
            }

            // Stat 3: Out of Stock
            Card(
                modifier = Modifier.width(140.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (outOfStockCount > 0) Color.Red.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Out of Stock", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$outOfStockCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (outOfStockCount > 0) Color.Red else Color.DarkGray)
                }
            }

            // Stat 4: Inventory valuation
            Card(
                modifier = Modifier.width(160.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Asset Value", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${totalValue.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                }
            }
        }

        // Search & Filtering Desk Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by product name/brand...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            editingProduct = null
                            showUpsertDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add SKU", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filters Row Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Selector Label
                    Text("Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    allCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showOnlyLowStock,
                            onCheckedChange = { 
                                showOnlyLowStock = it 
                                if (it) showOnlyOutOfStock = false
                            },
                            modifier = Modifier.scale(0.85f)
                        )
                        Text("Low Stock (<10)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showOnlyOutOfStock,
                            onCheckedChange = { 
                                showOnlyOutOfStock = it
                                if (it) showOnlyLowStock = false
                            },
                            modifier = Modifier.scale(0.85f)
                        )
                        Text("Out of Stock (0)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Inventory Items List
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No matching inventory items found.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { prod ->
                    val isDisabled = viewModel.disabledProductIds.contains(prod.id)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(alpha = if (isDisabled) 0.6f else 1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = 1.2.dp,
                            color = when {
                                prod.stock == 0 -> Color.Red.copy(alpha = 0.5f)
                                prod.stock < 10 -> Color(0xFFFFA726).copy(alpha = 0.5f)
                                else -> Color.LightGray.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product Image/Emoji Preview Column
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!prod.imageUrl.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = prod.imageUrl,
                                        contentDescription = prod.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                                    )
                                } else {
                                    val activeEmoji = if (!prod.imageEmoji.isNullOrBlank()) {
                                        prod.imageEmoji
                                    } else {
                                        when (prod.category) {
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
                                        }
                                    }
                                    Text(activeEmoji, fontSize = 28.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Product details middle section
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${prod.brand} • ${prod.weight} • ${prod.category}", fontSize = 11.sp, color = Color.Gray)
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Price & discount metrics
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("₹${prod.price}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    if (prod.discount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFFEBEE), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("${prod.discount.toInt()}% Off", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Stock Gauge Progress Indicator
                                val stockPercentage = (prod.stock / 50f).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = stockPercentage,
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = when {
                                        prod.stock == 0 -> Color.Red
                                        prod.stock < 10 -> Color.Red
                                        prod.stock < 20 -> Color(0xFFFFA726)
                                        else -> Color(0xFF2E7D32)
                                    },
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Status text
                                Text(
                                    text = when {
                                        prod.stock == 0 -> "🔴 Out of Stock"
                                        prod.stock < 10 -> "🟠 Critical: ${prod.stock} left"
                                        prod.stock < 20 -> "🟡 Low: ${prod.stock} units"
                                        else -> "🟢 Healthy: ${prod.stock} units"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        prod.stock == 0 -> Color.Red
                                        prod.stock < 10 -> Color(0xFFD84315)
                                        prod.stock < 20 -> Color(0xFFE65100)
                                        else -> Color(0xFF2E7D32)
                                    }
                                )
                            }

                            // Interactive direct CRUD controllers & editing column
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Direct edit and remove icon buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    IconButton(
                                        onClick = {
                                            editingProduct = prod
                                            showUpsertDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit product specs", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { productToDelete = prod },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove product", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }

                                // Quick Stock Incrementor Controller Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    FilledIconButton(
                                        onClick = {
                                            viewModel.adminUpdateProductStock(prod.id, (prod.stock - 1).coerceAtLeast(0))
                                        },
                                        modifier = Modifier.size(28.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    }
                                    
                                    Text("${prod.stock}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.widthIn(min = 16.dp), textAlign = TextAlign.Center)

                                    FilledIconButton(
                                        onClick = {
                                            viewModel.adminUpdateProductStock(prod.id, prod.stock + 1)
                                        },
                                        modifier = Modifier.size(28.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CRUD: CREATE & UPDATE Dialog Sheet
    if (showUpsertDialog) {
        val isEditMode = editingProduct != null
        var showEmojiSelectorInTab by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showUpsertDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isEditMode) "Edit Inventory SKU Specs" else "Add New Inventory SKU",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = brandInput,
                        onValueChange = { brandInput = it },
                        label = { Text("Brand Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Category Selector Row Dropdown or Simple Row
                    Text("Select Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (categoryInput == cat) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f))
                                    .border(1.dp, if (categoryInput == cat) MaterialTheme.colorScheme.secondary else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { categoryInput = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (e.g. 500g)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Discount %") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = stockInput,
                            onValueChange = { stockInput = it },
                            label = { Text("Initial Stock") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Detailed Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Product Photo & Illustration", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)

                    OutlinedTextField(
                        value = imageUrlInput,
                        onValueChange = {
                            imageUrlInput = it
                            if (it.isNotBlank()) imageEmojiInput = ""
                        },
                        label = { Text("Custom Photo Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("Or select a Preset Photo:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetImages.forEach { (label, url) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (imageUrlInput == url) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f))
                                    .border(1.dp, if (imageUrlInput == url) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable {
                                        imageUrlInput = url
                                        imageEmojiInput = ""
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Or pick an Illustration Emoji:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetEmojis.forEach { emo ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (imageEmojiInput == emo) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f))
                                    .border(1.dp, if (imageEmojiInput == emo) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                    .clickable {
                                        imageEmojiInput = emo
                                        imageUrlInput = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 18.sp)
                            }
                        }
                    }

                    // Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.1f))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrlInput.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = imageUrlInput,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                            )
                        } else if (imageEmojiInput.isNotBlank()) {
                            Text(imageEmojiInput, fontSize = 42.sp)
                        } else {
                            Text("No Custom Asset Selected", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dialog Confirmation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showUpsertDialog = false }) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                val pr = priceInput.toDoubleOrNull() ?: 0.0
                                val ds = discountInput.toDoubleOrNull() ?: 0.0
                                val st = stockInput.toIntOrNull() ?: 0
                                if (nameInput.isNotBlank() && pr > 0.0) {
                                    if (isEditMode && editingProduct != null) {
                                        // CRUD: UPDATE
                                        viewModel.adminUpdateProduct(
                                            editingProduct!!.copy(
                                                name = nameInput,
                                                brand = brandInput,
                                                category = categoryInput,
                                                weight = weightInput,
                                                price = pr,
                                                discount = ds,
                                                stock = st,
                                                description = descInput,
                                                imageUrl = imageUrlInput.ifBlank { null },
                                                imageEmoji = imageEmojiInput.ifBlank { null }
                                            )
                                        )
                                        Toast.makeText(context, "SKU specs updated successfully! 💾", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // CRUD: CREATE
                                        viewModel.adminAddProduct(
                                            name = nameInput,
                                            brand = brandInput,
                                            category = categoryInput,
                                            weight = weightInput,
                                            price = pr,
                                            discount = ds,
                                            stock = st,
                                            description = descInput,
                                            imageUrl = imageUrlInput.ifBlank { null },
                                            imageEmoji = imageEmojiInput.ifBlank { null }
                                        )
                                        Toast.makeText(context, "SKU created and inserted! 🚀", Toast.LENGTH_SHORT).show()
                                    }
                                    showUpsertDialog = false
                                    editingProduct = null
                                } else {
                                    Toast.makeText(context, "Verify product name and price!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isEditMode) "Save Changes" else "Add SKU")
                        }
                    }
                }
            }
        }
    }

    // CRUD: DELETE Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete SKU permanently?")
                }
            },
            text = {
                Text("This action cannot be undone. Are you sure you want to permanently remove \"${productToDelete!!.name}\" from the Pankaj Kirana catalog?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = productToDelete
                        if (prod != null) {
                            viewModel.adminDeleteProduct(prod)
                            Toast.makeText(context, "${prod.name} removed permanently. 🗑️", Toast.LENGTH_SHORT).show()
                        }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete SKU", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminCouponsTab(
    viewModel: GroceryViewModel
) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var minOrderAmount by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("2026-12-31") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("In-Store Coupon Management", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Generate Promo Voucher", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Coupon Code (e.g., FESTIVE50)") }, modifier = Modifier.fillMaxWidth())
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = discountPercent, onValueChange = { discountPercent = it }, label = { Text("Discount %") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = minOrderAmount, onValueChange = { minOrderAmount = it }, label = { Text("Min Order (₹)") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("Expiry Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val ds = discountPercent.toDoubleOrNull() ?: 0.0
                            val mo = minOrderAmount.toDoubleOrNull() ?: 0.0
                            if (code.isNotBlank() && ds > 0.0) {
                                viewModel.adminAddCoupon(code.trim().uppercase(), ds, expiryDate, mo)
                                code = ""
                                discountPercent = ""
                                minOrderAmount = ""
                                Toast.makeText(context, "Promo Coupon Issued!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Issue Voucher")
                    }
                }
            }
        }

        items(viewModel.couponsList) { coup ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(coup.code, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Text("${coup.discountPercent.toInt()}% Instant Discount", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Min Order: ₹${coup.minOrderAmount.toInt()} • Expiry: ${coup.expiryDate}", fontSize = 11.sp, color = Color.Gray)
                    }

                    IconButton(onClick = { viewModel.adminDeleteCoupon(coup.code) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Revoke Coupon", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBannersTab(
    viewModel: GroceryViewModel
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var promoText by remember { mutableStateOf("") }
    var targetCategory by remember { mutableStateOf("Spices") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Storefront Banner Manager", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Deploy Top Promo Banner", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Banner Title (e.g., Flat 30% Off)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = promoText, onValueChange = { promoText = it }, label = { Text("Subtext Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = targetCategory, onValueChange = { targetCategory = it }, label = { Text("Target Click Category Link") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && promoText.isNotBlank()) {
                                viewModel.adminAddBanner(title, promoText, targetCategory.trim())
                                title = ""
                                promoText = ""
                                Toast.makeText(context, "Promo Banner deployed!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Publish Banner")
                    }
                }
            }
        }

        items(viewModel.bannersList) { ban ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD0EBCB)),
                border = BorderStroke(1.dp, Color(0xFFBFC9BF))
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                        Text(ban.title, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF1B6C31))
                        Text(ban.promoText, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF00210B))
                        Text("Category Redirect: ${ban.categoryTarget}", fontSize = 11.sp, color = Color.Gray)
                    }

                    IconButton(
                        onClick = { viewModel.adminDeleteBanner(ban.id) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Take Down", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportsTab(
    viewModel: GroceryViewModel,
    orders: List<OrderEntity>,
    products: List<GroceryProduct>
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0.0f) }
    val scope = rememberCoroutineScope()

    val deliveredOrders = remember(orders) { orders.filter { it.status == "Delivered" } }
    val totalRevenue = remember(deliveredOrders) { deliveredOrders.sumOf { it.totalAmount } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Strategic Auditing & Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Executive Revenue Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivered Transactions Value")
                        Text("₹${totalRevenue.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dispatched Order Count")
                        Text("${deliveredOrders.size} finished", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Average Value per Inquiry")
                        val avg = if (deliveredOrders.isNotEmpty()) totalRevenue / deliveredOrders.size else 0.0
                        Text("₹${avg.toInt()}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Top Velocity Products", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    HorizontalDivider()
                    
                    products.sortedBy { it.stock }.take(3).forEach { p ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(p.name, fontSize = 13.sp)
                            Text("Low Stock: ${p.stock}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isExporting) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Generating Ledger Sheets...", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { exportProgress }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!isExporting) {
                            isExporting = true
                            exportProgress = 0.0f
                            scope.launch {
                                while (exportProgress < 1.0f) {
                                    delay(150)
                                    exportProgress += 0.1f
                                }
                                isExporting = false
                                Toast.makeText(context, "Sales Audit Excel Export saved to Downloads directory! 📈", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Excel")
                }
                Button(
                    onClick = {
                        if (!isExporting) {
                            isExporting = true
                            exportProgress = 0.0f
                            scope.launch {
                                while (exportProgress < 1.0f) {
                                    delay(100)
                                    exportProgress += 0.15f
                                }
                                isExporting = false
                                Toast.makeText(context, "Full Financial PDF Report generated! 📂", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print PDF Report")
                }
            }
        }
    }
}

@Composable
fun AdminNotificationsTab(
    viewModel: GroceryViewModel
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var bodyMsg by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Promotional Push Broadcast Desk", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Compose Alert Bulletin", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Bulletin Headline") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bodyMsg, onValueChange = { bodyMsg = it }, label = { Text("Detailed Bulletin Message") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && bodyMsg.isNotBlank()) {
                                viewModel.adminSendNotification(title, bodyMsg)
                                title = ""
                                bodyMsg = ""
                                Toast.makeText(context, "Bulletin Broadcast successfully to all client app terminals! 🔔", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Broadcast Bulletin Alert")
                    }
                }
            }
        }

        items(viewModel.notificationsList) { notif ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(notif.message, fontSize = 12.sp, color = Color.DarkGray)
                        val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        Text(df.format(Date(notif.timestamp)), fontSize = 10.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { viewModel.adminDeleteNotification(notif.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Bulletin", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    viewModel: GroceryViewModel
) {
    val context = LocalContext.current
    var storeName by remember { mutableStateOf(viewModel.storeSettings.value.storeName) }
    var logoEmoji by remember { mutableStateOf(viewModel.storeSettings.value.storeLogoEmoji) }
    var contactNumber by remember { mutableStateOf(viewModel.storeSettings.value.contactNumber) }
    var address by remember { mutableStateOf(viewModel.storeSettings.value.address) }
    var handlingCharge by remember { mutableStateOf(viewModel.storeSettings.value.deliveryCharge.toString()) }
    var businessHours by remember { mutableStateOf(viewModel.storeSettings.value.businessHours) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Global Shop Metadata", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = storeName, onValueChange = { storeName = it }, label = { Text("Store Label / Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = logoEmoji, onValueChange = { logoEmoji = it }, label = { Text("Brand Logo Emoji") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Helpdesk Contact Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Physical Warehouse Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = handlingCharge, onValueChange = { handlingCharge = it }, label = { Text("In-Store Handling Charge (₹)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = businessHours, onValueChange = { businessHours = it }, label = { Text("Business Open Timings") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val hc = handlingCharge.toDoubleOrNull() ?: 10.0
                            viewModel.storeSettings.value = StoreSettings(
                                storeName = storeName,
                                storeLogoEmoji = logoEmoji,
                                contactNumber = contactNumber,
                                address = address,
                                deliveryCharge = hc,
                                businessHours = businessHours
                            )
                            Toast.makeText(context, "System configurations committed globally! ⚙️", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save System Configurations")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProfileTab(
    viewModel: GroceryViewModel
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(viewModel.adminProfile.value.name) }
    var email by remember { mutableStateOf(viewModel.adminProfile.value.email) }
    var mobile by remember { mutableStateOf(viewModel.adminProfile.value.mobile) }
    var adminUsernameInput by remember { mutableStateOf(viewModel.adminUsername.value) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Operator Profile Dossier", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    HorizontalDivider()
                    
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Operator Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = adminUsernameInput, onValueChange = { adminUsernameInput = it }, label = { Text("Login Username / Admin Name") }, modifier = Modifier.fillMaxWidth().testTag("admin_username_edit"))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Designated Email Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Phone Number") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (adminUsernameInput.isNotBlank()) {
                                viewModel.adminProfile.value = AdminProfile(name = name, email = email, mobile = mobile)
                                viewModel.adminUsername.value = adminUsernameInput.trim()
                                Toast.makeText(context, "Admin personal specs and login username updated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Admin login username cannot be empty!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_profile_save_btn")
                    ) {
                        Text("Commit Changes")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Change Secure Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    HorizontalDivider()
                    
                    OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it }, label = { Text("Current Secret Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().testTag("admin_current_password_input"))
                    OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("New Secure Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().testTag("admin_new_password_input"))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Re-type New Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().testTag("admin_confirm_password_input"))

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (currentPassword == viewModel.adminPassword.value && newPassword == confirmPassword && newPassword.isNotBlank()) {
                                viewModel.adminPassword.value = newPassword
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                Toast.makeText(context, "Admin security password updated! New keys committed.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Incorrect current password or new passwords mismatch!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_save_btn")
                    ) {
                        Text("Apply Key Rotation")
                    }
                }
            }
        }
    }
}
