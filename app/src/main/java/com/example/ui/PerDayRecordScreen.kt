package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.data.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerDayRecordScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onDayClick: (String) -> Unit
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("user_credentials", android.content.Context.MODE_PRIVATE)
    val currencyCode = prefs.getString("currency", "NPR") ?: "NPR"
    val formatter = remember(currencyCode) {
        com.example.ui.CurrencyUtils.getFormatter(currencyCode)
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Group transactions by day
    val groupedTransactions = remember(transactions) {
        transactions.groupBy { dateFormat.format(Date(it.dateMillis)) }.toSortedMap(compareByDescending {
            dateFormat.parse(it)?.time ?: 0L
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Per Day Record") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            
            groupedTransactions.forEach { (dateStr, txs) ->
                item {
                    val dailyIncome = txs.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val dailyExpense = txs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    
                    val incStr = formatter.format(CurrencyUtils.convertFromNPR(dailyIncome, currencyCode))
                    val expStr = formatter.format(CurrencyUtils.convertFromNPR(dailyExpense, currencyCode))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDayClick(dateStr) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${txs.size} Transaction(s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("+$incStr", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                Text("-$expStr", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyDetailScreen(
    viewModel: FinanceViewModel,
    dateStr: String,
    onBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("user_credentials", android.content.Context.MODE_PRIVATE)
    val currencyCode = prefs.getString("currency", "NPR") ?: "NPR"
    val formatter = remember(currencyCode) {
        com.example.ui.CurrencyUtils.getFormatter(currencyCode)
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val dayTransactions = remember(transactions) {
        transactions.filter { dateFormat.format(Date(it.dateMillis)) == dateStr }
    }
    
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }

    if (fullScreenImageUri != null) {
        FullScreenImageDialog(imageUri = fullScreenImageUri!!) {
            fullScreenImageUri = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dateStr) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            
            items(dayTransactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tx.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tx.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = formatter.format(CurrencyUtils.convertFromNPR(tx.amount, currencyCode)),
                                color = if (tx.type == "INCOME") Color(0xFF4CAF50) else Color(0xFFF44336),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if (tx.description.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(text = tx.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (tx.note.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(text = "Note: ${tx.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (tx.imageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val uris = tx.imageUri.split(",").filter { it.isNotBlank() }
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uris) { uriStr ->
                                    AsyncImage(
                                        model = uriStr,
                                        contentDescription = "Receipt",
                                        modifier = Modifier
                                            .size(150.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { fullScreenImageUri = uriStr },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
