package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") } // INCOME or EXPENSE
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    var imageUris by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val createPhotoUri = {
        val tempFile = java.io.File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.provider"
        androidx.core.content.FileProvider.getUriForFile(context, authority, tempFile)
    }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            imageUris = imageUris + currentPhotoUri.toString()
        }
    }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newUris = uris.mapNotNull { uri ->
                val tempFile = java.io.File(context.cacheDir, "gallery_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.jpg")
                val authority = "${context.packageName}.provider"
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                androidx.core.content.FileProvider.getUriForFile(context, authority, tempFile).toString()
            }
            imageUris = imageUris + newUris
        }
    }

    val categoriesList by viewModel.getCategoriesByType(type).collectAsStateWithLifecycle(emptyList())
    val categories = categoriesList
        .distinctBy { it.name.trim().lowercase() }
        .sortedBy { if (it.name.trim().equals("other", ignoreCase = true)) 1 else 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (type == "INCOME") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { type = "INCOME"; selectedCategory = "" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Income",
                        color = if (type == "INCOME") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (type == "INCOME") FontWeight.Bold else FontWeight.Normal
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (type == "EXPENSE") MaterialTheme.colorScheme.error else Color.Transparent)
                        .clickable { type = "EXPENSE"; selectedCategory = "" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Expense",
                        color = if (type == "EXPENSE") MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (type == "EXPENSE") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it; if(amount.isNotBlank()) showError = false },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = showError && amount.isBlank(),
                supportingText = { if (showError && amount.isBlank()) Text("Amount is required") }
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; if(title.isNotBlank()) showError = false },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = showError && title.isBlank(),
                supportingText = { if (showError && title.isBlank()) Text("Title is required") }
            )

            val defaultCategoriesList = if (type == "INCOME") {
                listOf("Salary", "Freelancing", "Business", "Gift", "Investment", "Other")
            } else {
                listOf("Food", "Transport", "Shopping", "Education", "Health", "Entertainment", "Bills", "Rent", "Travel", "Other")
            }
            val displayCategories = if (categories.isEmpty()) defaultCategoriesList.map { com.example.data.Category(name = it, type = type) } else categories

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayCategories) { cat ->
                        val isSelected = selectedCategory == cat.name
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = cat.name }
                        ) {
                            Text(
                                text = com.example.ui.trans(cat.name),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                if (showError && selectedCategory.isBlank()) {
                    Text(
                        text = "Please select a category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                    )
                }
            }
            
            var customCategory by remember { mutableStateOf("") }
            if (selectedCategory == "Other") {
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("Custom Category (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Smart Note / Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            // Camera / Image placeholders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val uri = createPhotoUri()
                        currentPhotoUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = if (imageUris.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
                
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = if (imageUris.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
            
            if (imageUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(imageUris) { uriStr ->
                        Box(contentAlignment = Alignment.TopEnd) {
                            coil.compose.AsyncImage(
                                model = uriStr,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUris = imageUris.filter { it != uriStr } },
                                modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.error, androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Text("${imageUris.size} photo(s) attached", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (amount.isBlank() || title.isBlank() || selectedCategory.isBlank()) {
                        showError = true
                        android.widget.Toast.makeText(context, "Amount, Title, and Category are required", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val prefs = context.getSharedPreferences("user_credentials", android.content.Context.MODE_PRIVATE)
                    val currencyCode = prefs.getString("currency", "NPR") ?: "NPR"
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    val amountInNPR = com.example.ui.CurrencyUtils.convertToNPR(amountDouble, currencyCode)
                    
                    val finalCategory = if (selectedCategory == "Other") {
                        if (customCategory.trim().isEmpty()) "Other" else customCategory.trim()
                    } else if (selectedCategory.isEmpty()) "Other" else selectedCategory
                    
                    viewModel.addTransaction(
                        title = title,
                        amount = amountInNPR,
                        type = type,
                        categoryName = finalCategory,
                        desc = description,
                        note = note,
                        imageUri = if (imageUris.isNotEmpty()) imageUris.joinToString(",") else null,
                        dateMillis = System.currentTimeMillis()
                    )
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
