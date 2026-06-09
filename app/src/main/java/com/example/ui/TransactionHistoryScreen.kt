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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FullScreenImageDialog(imageUri: String, onDismiss: () -> Unit) {
    // Basic standard layout for fullscreen image
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Full Screen Receipt",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionHistoryScreen(viewModel: FinanceViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("user_credentials", android.content.Context.MODE_PRIVATE)
    val currencyCode = prefs.getString("currency", "NPR") ?: "NPR"
    val formatter = remember(currencyCode) {
        CurrencyUtils.getFormatter(currencyCode)
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    var selectedFilter by remember { mutableStateOf("7 Days") }
    val filters = listOf("7 Days", "1 Month", "1 Year", "All")

    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }

    val selectedIds = remember { mutableStateListOf<Int>() }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<List<Int>?>(null) }

    if (fullScreenImageUri != null) {
        FullScreenImageDialog(imageUri = fullScreenImageUri!!) {
            fullScreenImageUri = null
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedIds.isNotEmpty()) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds.clear() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            selectedIds.clear()
                            val filteredList = transactions.filter { tx ->
                                val diffDays = (System.currentTimeMillis() - tx.dateMillis) / (1000 * 60 * 60 * 24)
                                when (selectedFilter) {
                                    "7 Days" -> diffDays <= 7
                                    "1 Month" -> diffDays <= 30
                                    "1 Year" -> diffDays <= 365
                                    else -> true
                                }
                            }
                            selectedIds.addAll(filteredList.map { it.id })
                        }) {
                            Text(trans("Select All"))
                        }
                        IconButton(onClick = { showDeleteConfirm = selectedIds.toList() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            val filteredList = transactions.filter { tx ->
                val diffDays = (System.currentTimeMillis() - tx.dateMillis) / (1000 * 60 * 60 * 24)
                when (selectedFilter) {
                    "7 Days" -> diffDays <= 7
                    "1 Month" -> diffDays <= 30
                    "1 Year" -> diffDays <= 365
                    else -> true
                }
            }.sortedByDescending { it.dateMillis }
            
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { tx ->
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
                                        } else {
                                            selectedTransaction = tx
                                        }
                                    },
                                    onLongClick = {
                                        if (selectedIds.isEmpty()) {
                                            selectedIds.add(tx.id)
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    if (selectedIds.isNotEmpty()) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { chk ->
                                                if (chk) selectedIds.add(tx.id) else selectedIds.remove(tx.id)
                                            },
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    if (tx.imageUri != null) {
                                        AsyncImage(
                                            model = tx.imageUri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
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
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(tx.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(trans(tx.categoryName) + " • " + dateFormat.format(Date(tx.dateMillis)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = sign + formatter.format(CurrencyUtils.convertFromNPR(tx.amount, currencyCode)),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                    if (selectedIds.isEmpty()) {
                                        Row(horizontalArrangement = Arrangement.End) {
                                            IconButton(onClick = { editingTransaction = tx }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { showDeleteConfirm = listOf(tx.id) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (selectedTransaction != null) {
        val tx = selectedTransaction!!
        AlertDialog(
            onDismissRequest = { selectedTransaction = null },
            title = { Text(tx.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(trans("Amount") + ": ${formatter.format(CurrencyUtils.convertFromNPR(tx.amount, currencyCode))}", fontWeight = FontWeight.Bold)
                    Text(trans("Type") + ": ${trans(tx.type)}")
                    Text(trans("Category") + ": ${trans(tx.categoryName)}")
                    Text(trans("Date") + ": ${dateFormat.format(Date(tx.dateMillis))}")
                    if (tx.description.isNotBlank()) {
                        Text(trans("Description") + ": ${tx.description}")
                    }
                    if (tx.note.isNotBlank()) {
                        Text(trans("Note") + ": ${tx.note}")
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
            },
            confirmButton = {
                TextButton(onClick = { selectedTransaction = null }) {
                    Text(trans("Close"))
                }
            }
        )
    }

    if (showDeleteConfirm != null) {
        val idsToDelete = showDeleteConfirm!!
        val messageMoved = trans("Moved to Bin")
        val actionUndo = trans("Undo")
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(trans("Sure, are you want to delete?")) },
            text = { Text(trans("This will move the selected items to the Bin.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.moveToBin(idsToDelete)
                        showDeleteConfirm = null
                        selectedIds.clear()
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = messageMoved,
                                actionLabel = actionUndo,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreFromBin(idsToDelete)
                            }
                        }
                    }
                ) {
                    Text(trans("Yes"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text(trans("No"))
                }
            }
        )
    }

    if (editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!,
            onDismiss = { editingTransaction = null },
            onSave = { updatedTx ->
                viewModel.updateTransaction(updatedTx)
                editingTransaction = null
            }
        )
    }
}
