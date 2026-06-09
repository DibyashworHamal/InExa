package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BinScreen(viewModel: FinanceViewModel, onBack: () -> Unit) {
    val binTransactions by viewModel.binTransactions.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("user_credentials", android.content.Context.MODE_PRIVATE)
    val currencyCode = prefs.getString("currency", "NPR") ?: "NPR"
    val formatter = remember(currencyCode) {
        CurrencyUtils.getFormatter(currencyCode)
    }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    val selectedIds = remember { mutableStateListOf<Int>() }
    var showDeleteConfirm by remember { mutableStateOf<List<Int>?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedIds.isNotEmpty()) "${selectedIds.size} selected" else trans("Bin")) },
                navigationIcon = {
                    if (selectedIds.isNotEmpty()) {
                        IconButton(onClick = { selectedIds.clear() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Clear selection")
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (binTransactions.isNotEmpty()) {
                        if (selectedIds.size < binTransactions.size) {
                            TextButton(onClick = { 
                                selectedIds.clear()
                                selectedIds.addAll(binTransactions.map { it.id })
                            }) {
                                Text(trans("Select All"))
                            }
                        } else {
                            TextButton(onClick = { selectedIds.clear() }) {
                                Text(trans("Deselect All"))
                            }
                        }
                    }
                    if (selectedIds.isNotEmpty()) {
                        IconButton(onClick = { viewModel.restoreFromBin(selectedIds.toList()); selectedIds.clear() }) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore")
                        }
                        IconButton(onClick = { showDeleteConfirm = selectedIds.toList() }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Permanently Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (binTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(trans("Bin is empty"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                Text(
                    text = trans("the item must be reside here for 30 days only, after 30 days the item will be deleted automatically"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(binTransactions, key = { it.id }) { tx ->
                        val isIncome = tx.type == "INCOME"
                        val color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626)
                        val bgColor = if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                        val sign = if (isIncome) "+" else "-"
                        val isSelected = selectedIds.contains(tx.id)
    
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (selectedIds.isNotEmpty()) {
                                            if (isSelected) selectedIds.remove(tx.id) else selectedIds.add(tx.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (selectedIds.isEmpty()) {
                                            selectedIds.add(tx.id)
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedIds.isNotEmpty()) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { chk ->
                                        if (chk) selectedIds.add(tx.id) else selectedIds.remove(tx.id)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(trans(tx.categoryName) + " • " + dateFormat.format(Date(tx.dateMillis)), style = MaterialTheme.typography.labelMedium)
                            }
                            Text(
                                text = sign + formatter.format(CurrencyUtils.convertFromNPR(tx.amount, currencyCode)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        } // end Row
                    } // end Card
                } // end items
            } // end LazyColumn
        } // end Column
        } // end else
        
        if (showDeleteConfirm != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = { Text(trans("Confirm Permanent Delete")) },
                text = { Text(trans("Are you sure you want to permanently delete these items? This action cannot be undone.")) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deletePermanently(showDeleteConfirm!!)
                        selectedIds.clear()
                        showDeleteConfirm = null
                    }) {
                        Text(trans("Delete Permanently"), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) {
                        Text(trans("Cancel"))
                    }
                }
            )
        }
    }
}
