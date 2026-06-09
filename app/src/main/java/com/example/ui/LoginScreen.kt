package com.example.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var rememberMe by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetUsername by remember { mutableStateOf("") }
    var resetFavoriteNumber by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var resetConfirmPassword by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }
    var resetSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val savedUsername = prefs.getString("remembered_username", "")
        if (!savedUsername.isNullOrEmpty()) {
            username = savedUsername
            rememberMe = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppLogo(modifier = Modifier.padding(bottom = 16.dp), isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme())
            
            Text(
                " Sign in to manage your Income and Expanses smartly with InExa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it 
                    errorMessage = null
                },
                label = { Text("Username (Full Name)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it 
                    errorMessage = null
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text("Remember me", style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = { showForgotDialog = true }) {
                    Text("Forgot Password?")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val trimmedUsername = username.trim()
                    val trimmedPassword = password.trim()

                    if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
                        errorMessage = "Please enter both username and password"
                        return@Button
                    }

                    val prefs = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                    val registeredPassword = prefs.getString(trimmedUsername, null)

                    if (registeredPassword == null) {
                        errorMessage = "Invalid username or password"
                    } else if (registeredPassword != trimmedPassword) {
                        errorMessage = "Invalid username or password"
                    } else {
                        if (rememberMe) {
                            prefs.edit().putString("remembered_username", trimmedUsername).apply()
                        } else {
                            prefs.edit().remove("remembered_username").apply()
                        }
                        prefs.edit().putString("current_user", trimmedUsername).putBoolean("just_logged_in", true).apply()
                        android.widget.Toast.makeText(context, "Login successful", android.widget.Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "Sign In",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = onNavigateToSignup) {
                Text("Don't have an account? Sign up")
            }
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (resetError != null) {
                        Text(text = resetError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (resetSuccess) {
                        Text(text = "Password reset successfully!", color = MaterialTheme.colorScheme.primary)
                    } else {
                        OutlinedTextField(
                            value = resetUsername,
                            onValueChange = { resetUsername = it; resetError = null },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = resetFavoriteNumber,
                            onValueChange = { resetFavoriteNumber = it; resetError = null },
                            label = { Text("What is your favorite number?") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = resetNewPassword,
                            onValueChange = { resetNewPassword = it; resetError = null },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation()
                        )
                        OutlinedTextField(
                            value = resetConfirmPassword,
                            onValueChange = { resetConfirmPassword = it; resetError = null },
                            label = { Text("Confirm New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }
                }
            },
            confirmButton = {
                if (resetSuccess) {
                    TextButton(onClick = { showForgotDialog = false; resetSuccess = false }) {
                        Text("Close")
                    }
                } else {
                    TextButton(onClick = {
                        val trimmedUsername = resetUsername.trim()
                        val prefs = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                        val storedFavNum = prefs.getString("${trimmedUsername}_fav_num", null)
                        
                        if (storedFavNum == null) {
                            resetError = "User not found"
                        } else if (storedFavNum != resetFavoriteNumber.trim()) {
                            resetError = "Incorrect favorite number"
                        } else if (resetNewPassword != resetConfirmPassword) {
                            resetError = "Passwords do not match"
                        } else if (resetNewPassword.length < 8 || !resetNewPassword.any { it.isDigit() } || !resetNewPassword.any { it.isUpperCase() } || !resetNewPassword.any { it.isLowerCase() }) {
                            resetError = "Password must be at least 8 characters, include a digit, uppercase and lowercase letter."
                        } else {
                            prefs.edit().putString(trimmedUsername, resetNewPassword).apply()
                            resetSuccess = true
                        }
                    }) {
                        Text("Reset Password")
                    }
                }
            },
            dismissButton = {
                if (!resetSuccess) {
                    TextButton(onClick = { showForgotDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
