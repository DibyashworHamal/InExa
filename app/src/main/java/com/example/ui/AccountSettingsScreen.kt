package com.example.ui

import android.content.Context
import android.app.Activity
import android.graphics.Bitmap
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onThemeToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
    val currentUser = prefs.getString("current_user", "User") ?: "User"

    var profilePicUri by remember { mutableStateOf(prefs.getString("${currentUser}_profile_pic", null)) }
    var bio by remember { mutableStateOf(prefs.getString("${currentUser}_bio", "")) }
    var showBioDialog by remember { mutableStateOf(false) }
    var newBio by remember { mutableStateOf(bio ?: "") }

    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("${currentUser}_notifications", true)) }
    var darkThemeEnabled by remember { mutableStateOf(prefs.getBoolean("dark_theme", false)) }
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var currentCropUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val finalUri = currentCropUri
            if (finalUri != null) {
                profilePicUri = finalUri.toString()
                prefs.edit().putString("${currentUser}_profile_pic", profilePicUri).apply()
                Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val authority = "${context.packageName}.provider"
                
                val tempInputFile = java.io.File(context.cacheDir, "temp_input.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempInputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val inputUri = androidx.core.content.FileProvider.getUriForFile(context, authority, tempInputFile)

                val tempOutputFile = java.io.File(context.cacheDir, "crop_output_${System.currentTimeMillis()}.jpg")
                val outputUri = androidx.core.content.FileProvider.getUriForFile(context, authority, tempOutputFile)
                
                currentCropUri = android.net.Uri.fromFile(tempOutputFile)

                val cropIntent = Intent("com.android.camera.action.CROP").apply {
                    setDataAndType(inputUri, "image/*")
                    putExtra("crop", "true")
                    putExtra("aspectX", 1)
                    putExtra("aspectY", 1)
                    putExtra("outputX", 512)
                    putExtra("outputY", 512)
                    putExtra("return-data", false)
                    putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                
                val resInfoList = context.packageManager.queryIntentActivities(cropIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.grantUriPermission(packageName, inputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                cropLauncher.launch(cropIntent)
            } catch (e: Exception) {
                // Crop failed/unsupported, just use original
                profilePicUri = uri.toString()
                prefs.edit().putString("${currentUser}_profile_pic", profilePicUri).apply()
                Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(48.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Account Settings", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        launcher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (profilePicUri != null) {
                    AsyncImage(
                        model = profilePicUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = currentUser.firstOrNull()?.toString()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentUser,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!bio.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bio ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Theme", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = darkThemeEnabled,
                            onCheckedChange = { 
                                darkThemeEnabled = it
                                prefs.edit().putBoolean("dark_theme", it).apply()
                                onThemeToggle(it)
                            }
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notifications", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { 
                                notificationsEnabled = it
                                prefs.edit().putBoolean("${currentUser}_notifications", it).apply()
                                Toast.makeText(context, if (it) "Notifications Enabled" else "Notifications Disabled", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Localization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var languageExpanded by remember { mutableStateOf(false) }
                    val languageOptions = listOf("English(US)", "Nepali", "Spanish", "French", "Hindi")
                    var selectedLanguage by remember { mutableStateOf(prefs.getString("language", "English(US)") ?: "English(US)") }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { languageExpanded = true }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Language", style = MaterialTheme.typography.bodyLarge)
                        Box {
                            Text(selectedLanguage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                            DropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                                languageOptions.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang) },
                                        onClick = {
                                            selectedLanguage = lang
                                            prefs.edit().putString("language", lang).apply()
                                            languageExpanded = false
                                            val loc = when(lang) {
                                                "Nepali" -> java.util.Locale("ne", "NP")
                                                "Spanish" -> java.util.Locale("es", "ES")
                                                "French" -> java.util.Locale("fr", "FR")
                                                "Hindi" -> java.util.Locale("hi", "IN")
                                                else -> java.util.Locale.US
                                            }
                                            java.util.Locale.setDefault(loc)
                                            context.resources.configuration.setLocale(loc)
                                            context.resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)
                                            Toast.makeText(context, "Language changed to $lang", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    var currencyExpanded by remember { mutableStateOf(false) }
                    val currencyOptions = listOf("NPR", "USD", "EUR", "GBP", "INR", "AUD", "CAD")
                    var selectedCurrency by remember { mutableStateOf(prefs.getString("currency", "NPR") ?: "NPR") }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { currencyExpanded = true }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Currency", style = MaterialTheme.typography.bodyLarge)
                        Box {
                            Text(selectedCurrency, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                            DropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                                currencyOptions.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr) },
                                        onClick = {
                                            selectedCurrency = curr
                                            prefs.edit().putString("currency", curr).apply()
                                            currencyExpanded = false
                                            Toast.makeText(context, "Currency changed to $curr", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().clickable { showPasswordDialog = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Change Password", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    onLogout()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Logout", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showBioDialog) {
        AlertDialog(
            onDismissRequest = { showBioDialog = false },
            title = { Text("Edit Bio") },
            text = {
                OutlinedTextField(
                    value = newBio,
                    onValueChange = { newBio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    bio = newBio
                    prefs.edit().putString("${currentUser}_bio", newBio).apply()
                    showBioDialog = false
                    Toast.makeText(context, "Bio updated", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBioDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text(com.example.ui.trans("Change Password")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text(com.example.ui.trans("Old Password")) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(com.example.ui.trans("New Password")) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = { Text(com.example.ui.trans("Confirm New Password")) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val currentPass = prefs.getString(currentUser, "")
                    if (currentPass == oldPassword) {
                        if (newPassword != confirmNewPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
                        } else if (newPassword.length >= 8 && newPassword.any { it.isDigit() } && newPassword.any { it.isUpperCase() } && newPassword.any { it.isLowerCase() }) {
                            prefs.edit().putString(currentUser, newPassword).apply()
                            showPasswordDialog = false
                            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Password must be at least 8 characters, include a digit, uppercase and lowercase letter.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Incorrect old password", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text(com.example.ui.trans("Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text(com.example.ui.trans("Cancel"))
                }
            }
        )
    }
}
