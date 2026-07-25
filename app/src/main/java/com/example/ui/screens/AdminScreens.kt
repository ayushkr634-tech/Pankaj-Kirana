package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GroceryProduct
import com.example.data.OrderEntity
import com.example.data.UserProfileEntity
import com.example.ui.GroceryViewModel
import com.example.ui.components.SalesLineChart
import com.example.ui.components.SalesPieChart
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: GroceryViewModel,
    orders: List<OrderEntity>,
    products: List<GroceryProduct>,
    modifier: Modifier = Modifier,
) {
    var selectedAdminTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Products, 2: Orders, 3: Customers

    val stats = viewModel.getAdminStats(orders, products)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pankaj Kirana - Admin Desk") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = "HOME" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Admin bottom/top sub-navigation
            TabRow(selectedTabIndex = selectedAdminTab) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text("Overview") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    modifier = Modifier.testTag("admin_tab_overview")
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text("Products") },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    modifier = Modifier.testTag("admin_tab_products")
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text("Orders") },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                    modifier = Modifier.testTag("admin_tab_orders")
                )
                Tab(
                    selected = selectedAdminTab == 3,
                    onClick = { selectedAdminTab = 3 },
                    text = { Text("Customers") },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    modifier = Modifier.testTag("admin_tab_customers")
                )
            }

            when (selectedAdminTab) {
                0 -> AdminOverviewTab(stats, orders, products)
                1 -> AdminProductsTab(viewModel, products)
                2 -> AdminOrdersTab(viewModel, orders)
                3 -> AdminCustomersTab()
            }
        }
    }
}

// --- TAB 1: OVERVIEW METRICS ---
@Composable
fun AdminOverviewTab(
    stats: com.example.ui.AdminStats,
    orders: List<OrderEntity>,
    products: List<GroceryProduct>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Store Revenue Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        // Large stats row cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Revenue", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${stats.totalRevenue.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Delivered Orders", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${stats.deliveredOrdersCount} Tickets", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pending Orders", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${stats.pendingOrdersCount} Active", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFD4AF37))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Low Stock Items", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${stats.lowStockCount} Items", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Red)
                }
            }
        }

        // Low stock item list trigger alerts
        val lowStockProducts = products.filter { it.stock < 10 }
        if (lowStockProducts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Alert", tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inventory Alert: Low Stock Warning!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Red)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    lowStockProducts.take(3).forEach { prod ->
                        Text("• ${prod.name} (Only ${prod.stock} bags remaining)", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pie chart for orders breakdown
        Text("Order Delivery Share", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        SalesPieChart(
            deliveredCount = stats.deliveredOrdersCount,
            pendingCount = stats.pendingOrdersCount,
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Line chart for trends
        val mockTrendData = listOf(
            stats.totalRevenue * 0.4,
            stats.totalRevenue * 0.6,
            stats.totalRevenue * 0.5,
            stats.totalRevenue * 0.7,
            stats.totalRevenue * 0.9,
            stats.totalRevenue
        )
        SalesLineChart(
            weeklySales = mockTrendData,
            modifier = Modifier.fillMaxWidth().height(160.dp)
        )
    }
}

// --- TAB 2: INVENTORY & PRODUCT MANAGEMENT ---
@Composable
fun AdminProductsTab(
    viewModel: GroceryViewModel,
    products: List<GroceryProduct>
) {
    val context = LocalContext.current
    var showAddForm by remember { mutableStateOf(value = false) }

    // Add Form Fields
    var fName by remember { mutableStateOf("") }
    var fBrand by remember { mutableStateOf("") }
    var fCategory by remember { mutableStateOf("Rice & Flour") }
    var fWeight by remember { mutableStateOf("") }
    var fPrice by remember { mutableStateOf("") }
    var fDiscount by remember { mutableStateOf("0") }
    var fStock by remember { mutableStateOf("") }
    var fDescription by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Store Catalog", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { showAddForm = !showAddForm },
                modifier = Modifier.testTag("admin_toggle_add_form_btn")
            ) {
                Icon(if (showAddForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showAddForm) "Hide Form" else "Add Product")
            }
        }

        if (showAddForm) {
            // Add product form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = fName,
                    onValueChange = { fName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_p_name"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fBrand,
                        onValueChange = { fBrand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f).testTag("add_p_brand"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fWeight,
                        onValueChange = { fWeight = it },
                        label = { Text("Weight (e.g. 5 kg)") },
                        modifier = Modifier.weight(1f).testTag("add_p_weight"),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fPrice,
                        onValueChange = { fPrice = it },
                        label = { Text("Price (INR)") },
                        modifier = Modifier.weight(1f).testTag("add_p_price"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fDiscount,
                        onValueChange = { fDiscount = it },
                        label = { Text("Discount (%)") },
                        modifier = Modifier.weight(1f).testTag("add_p_discount"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fStock,
                        onValueChange = { fStock = it },
                        label = { Text("Stock Qty") },
                        modifier = Modifier.weight(1f).testTag("add_p_stock"),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = fDescription,
                    onValueChange = { fDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().testTag("add_p_desc")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val pPrice = fPrice.toDoubleOrNull() ?: 0.0
                        val pStock = fStock.toIntOrNull() ?: 0
                        val pDisc = fDiscount.toDoubleOrNull() ?: 0.0

                        if (fName.isNotBlank() && fBrand.isNotBlank() && fWeight.isNotBlank() && (pPrice > 0)) {
                            viewModel.adminAddProduct(
                                name = fName, brand = fBrand, category = fCategory, weight = fWeight,
                                price = pPrice, discount = pDisc, stock = pStock, description = fDescription
                            )
                            fName = ""
                            fBrand = ""
                            fWeight = ""
                            fPrice = ""
                            fDiscount = "0"
                            fStock = ""
                            fDescription = ""
                            showAddForm = false
                            Toast.makeText(context, "New Product added to Catalog! 🎉", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please complete fields with valid values", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_p_submit")
                ) {
                    Text("Save & Publish Product")
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
            }
        }

        // Inventory scrollable list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { prod ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_p_row_${prod.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Brand: ${prod.brand} | Category: ${prod.category}", fontSize = 11.sp, color = Color.Gray)
                            Text("Price: ₹${prod.price.toInt()} | Discount: ${prod.discount.toInt()}%", fontSize = 11.sp, color = Color.Gray)
                        }

                        // Stock editor inline
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { viewModel.adminUpdateProductStock(prod.id, (prod.stock - 5).coerceAtLeast(0)) }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color.Gray)
                            }
                            Text(prod.stock.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { viewModel.adminUpdateProductStock(prod.id, prod.stock + 5) }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.adminDeleteProduct(prod) }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: ORDER TICKETS MANAGEMENT ---
@Composable
fun AdminOrdersTab(
    viewModel: GroceryViewModel,
    orders: List<OrderEntity>
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(12.dp))
                Text("No customer orders placed yet!", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(orders) { ord ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_ord_row_${ord.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order #${ord.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(dateFormat.format(Date(ord.timestamp)), fontSize = 11.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Delivery Mode: ${ord.deliveryTime}", fontSize = 12.sp, color = Color.Gray)
                    Text("Address: ${ord.deliveryAddress}", fontSize = 12.sp, color = Color.DarkGray)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total Revenue: ₹${ord.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    when (ord.status) {
                                        "Order Confirmed" -> Color.Blue.copy(alpha = 0.1f)
                                        "Packing" -> Color.Yellow.copy(alpha = 0.2f)
                                        "Out for Delivery" -> Color.Magenta.copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = ord.status,
                                color = when (ord.status) {
                                    "Order Confirmed" -> Color.Blue
                                    "Packing" -> MaterialTheme.colorScheme.secondary
                                    "Out for Delivery" -> Color.Magenta
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Order status quick advancements controls
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val nextStages = listOf("Order Confirmed", "Packing", "Out for Delivery", "Delivered")
                            nextStages.forEach { stage ->
                                if (stage != ord.status) {
                                    IconButton(
                                        onClick = {
                                            viewModel.adminUpdateOrderStatus(ord.id, stage)
                                            Toast.makeText(context, "Status updated to $stage", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp).testTag("status_set_${stage.take(3)}_${ord.id}")
                                    ) {
                                        Icon(
                                            imageVector = when(stage) {
                                                "Packing" -> Icons.Default.Inventory
                                                "Out for Delivery" -> Icons.Default.LocalShipping
                                                "Delivered" -> Icons.Default.CheckCircle
                                                else -> Icons.Default.Check
                                            },
                                            contentDescription = stage,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
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
}

// --- TAB 4: CUSTOMER PROFILES LIST ---
@Composable
fun AdminCustomersTab() {
    val context = LocalContext.current
    val customersList = remember {
        mutableStateListOf(
            UserProfileEntity(id = 1, name = "Ayush Kumar", email = "ayushkr634@gmail.com", mobile = "8235091376", loyaltyPoints = 250, referralCode = "PKAYUSH99"),
            UserProfileEntity(id = 2, name = "Rahul Sharma", email = "rahul@gmail.com", mobile = "9876543210", loyaltyPoints = 120, referralCode = "PKRAHUL12"),
            UserProfileEntity(id = 3, name = "Priya Gupta", email = "priya@gmail.com", mobile = "9123456789", loyaltyPoints = 340, referralCode = "PKPRIYA77")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(customersList) { cust ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PersonPin, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Mobile: ${cust.mobile} | Email: ${cust.email}", fontSize = 11.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "${cust.name} profile status updated!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Verify Client", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
