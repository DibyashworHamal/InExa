package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Transaction
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: FinanceViewModel, onNavigateToAdd: () -> Unit, onNavigateToAnalytics: () -> Unit = {}, onNavigateToPerDayRecord: () -> Unit = {}) {
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val monthlyIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val monthlyExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("user_credentials", android.content.Context.MODE_PRIVATE)
    
    val currentUser = prefs.getString("current_user", "") ?: ""
    val notificationsEnabled = prefs.getBoolean("${currentUser}_notifications", true)
    
    var showBudgetAlert by remember { mutableStateOf(false) }

    LaunchedEffect(monthlyExpense, monthlyIncome) {
        if (monthlyExpense > monthlyIncome && prefs.getBoolean("just_logged_in", false)) {
            if (notificationsEnabled) {
                showBudgetAlert = true
            }
            prefs.edit().putBoolean("just_logged_in", false).apply()
        }
    }

    if (showBudgetAlert) {
        AlertDialog(
            onDismissRequest = { showBudgetAlert = false },
            title = { Text("Budget Exceeded!") },
            text = { Text("Your expenses have exceeded your income. Please check your notifications for more details.") },
            confirmButton = {
                TextButton(onClick = { showBudgetAlert = false }) {
                    Text("OK")
                }
            }
        )
    }

    val currencyCode = prefs.getString("currency", "NPR") ?: "NPR"
    val formatter = remember(currencyCode) {
        com.example.ui.CurrencyUtils.getFormatter(currencyCode)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(4.dp, Color.White, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BalanceCard(balance = CurrencyUtils.convertFromNPR(totalBalance, currencyCode), formatter = formatter)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = com.example.ui.trans("Income"),
                        amount = CurrencyUtils.convertFromNPR(monthlyIncome, currencyCode),
                        icon = Icons.Default.TrendingUp,
                        iconColor = Color(0xFF001D36),
                        formatter = formatter,
                        modifier = Modifier.weight(1f),
                        isSecondary = true
                    )
                    SummaryCard(
                        title = com.example.ui.trans("Expense"),
                        amount = CurrencyUtils.convertFromNPR(monthlyExpense, currencyCode),
                        icon = Icons.Default.TrendingDown,
                        iconColor = Color(0xFF001D36),
                        formatter = formatter,
                        modifier = Modifier.weight(1f),
                        isSecondary = false
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(24.dp)).clickable { onNavigateToAnalytics() }.padding(16.dp)) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Text("ANALYTICS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Row(modifier = Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight(0.3f).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight(0.7f).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight(0.5f).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight(0.9f).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(24.dp)).clickable { onNavigateToPerDayRecord() }) {
                        coil.compose.AsyncImage(
                            model = "https://images.unsplash.com/photo-1544377193-33dcf4d68fb5?w=400&q=80",
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Per Day Record", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.ui.trans("Recent Activities"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        transactions.take(10).forEach { tx ->
                            TransactionItem(transaction = tx, formatter = formatter, currencyCode = currencyCode)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (transactions.isEmpty()) {
                            Text(com.example.ui.trans("No recent activities"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, formatter: NumberFormat) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0061A4),
                        Color(0xFF1E4D8E)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                com.example.ui.trans("Total Balance"),
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                formatter.format(balance),
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    formatter: NumberFormat,
    modifier: Modifier = Modifier,
    isSecondary: Boolean = false
) {
    val bgColor = if (isSecondary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSecondary) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
    val border = if (!isSecondary) BorderStroke(1.dp, Color(0xFFF1F5F9)) else null

    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(title.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.6f))
                Icon(icon, contentDescription = null, tint = contentColor.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    formatter.format(amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(if (isSecondary) "Savings Growth" else "Recent Total", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, formatter: java.text.NumberFormat, currencyCode: String) {
    val isIncome = transaction.type == "INCOME"
    val color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626) // Tailwind green-600 / red-600
    val bgColor = if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2) // Tailwind green-100 / red-100
    val sign = if (isIncome) "+" else "-"
    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(transaction.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(com.example.ui.trans(transaction.categoryName) + " • " + dateFormat.format(java.util.Date(transaction.dateMillis)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
        Text(
            text = sign + formatter.format(CurrencyUtils.convertFromNPR(transaction.amount, currencyCode)),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
