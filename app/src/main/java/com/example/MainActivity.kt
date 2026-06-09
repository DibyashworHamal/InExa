package com.example

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.ui.*
import com.example.ui.theme.FinanceAppTheme
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val prefs = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val lang = prefs.getString("language", "English(US)") ?: "English(US)"
            val loc = when(lang) {
                "Nepali" -> java.util.Locale("ne", "NP")
                "Spanish" -> java.util.Locale("es", "ES")
                "French" -> java.util.Locale("fr", "FR")
                "Hindi" -> java.util.Locale("hi", "IN")
                else -> java.util.Locale.US
            }
            java.util.Locale.setDefault(loc)
            resources.configuration.setLocale(loc)
            resources.updateConfiguration(resources.configuration, resources.displayMetrics)

            enableEdgeToEdge()
            setContent {
                val context = androidx.compose.ui.platform.LocalContext.current
                var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("dark_theme", false)) }
                
                FinanceAppTheme(darkTheme = isDarkTheme) {
                    MainApp(onThemeToggle = { isDarkTheme = it })
                }
            }
        } catch (e: Exception) {
            setContent { 
                MaterialTheme {
                    androidx.compose.material3.Text("CRASH INITIAL: ${e.stackTraceToString()}", color = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(onThemeToggle: (Boolean) -> Unit) {
    val navController = rememberNavController()

    val financeViewModel: FinanceViewModel = viewModel()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 })

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val context = androidx.compose.ui.platform.LocalContext.current

    val items = listOf(
        Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
        Triple("history", "History", Icons.Default.ReceiptLong),
        Triple("notifications", "Notifications", Icons.Default.Notifications),
        Triple("notes", "Keep Notes", Icons.Default.EditNote)
    )

    val drawerItems = listOf(
        Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
        Triple("analytics", "Analytics", Icons.Default.PieChart),
        Triple("account_settings", "Profile", Icons.Default.Person),
        Triple("bin", "Bin", Icons.Default.Delete),
        Triple("about", "About", Icons.Default.Info)
    )

    val expense = financeViewModel.monthlyExpense.collectAsStateWithLifecycle().value
    val income = financeViewModel.monthlyIncome.collectAsStateWithLifecycle().value
    val outOfBudget = expense > income

    val isAuthScreen = currentDestination?.route in listOf("splash", "login", "signup")

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthScreen && (drawerState.isOpen || (currentDestination?.route == "dashboard" && pagerState.currentPage == 0)),
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                drawerItems.forEach { (route, name, icon) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(com.example.ui.trans(name)) },
                        selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (route == "dashboard") {
                                scope.launch { pagerState.scrollToPage(0) }
                            }
                            navController.navigate(route) {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    label = { Text(com.example.ui.trans("Logout"), color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        val prefs = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                        prefs.edit().remove("current_user").apply()
                        android.widget.Toast.makeText(context, "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                val isTopLevel = currentDestination?.route == "dashboard"
                if (isTopLevel) {
                    val titleText = when (pagerState.currentPage) {
                        0 -> "InExa"
                        1 -> "History"
                        2 -> "Notifications"
                        3 -> "Keep Notes"
                        else -> "InExa"
                    }
                    TopAppBar(
                        title = { com.example.ui.DesignedText(text = com.example.ui.trans(titleText), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val prefs = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                            val currentUser = prefs.getString("current_user", "User") ?: "User"
                            val profilePicUri = prefs.getString("${currentUser}_profile_pic", null)
                            
                            IconButton(onClick = { navController.navigate("account_settings") }) {
                                if (profilePicUri != null) {
                                    AsyncImage(
                                        model = profilePicUri, 
                                        contentDescription = "Profile", 
                                        modifier = Modifier.size(32.dp).clip(CircleShape), 
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.AccountCircle, 
                                        contentDescription = "Profile", 
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                val isTopLevel = currentDestination?.route == "dashboard"
                if (isTopLevel) {
                    NavigationBar(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        items.forEachIndexed { index, (route, name, icon) ->
                            NavigationBarItem(
                                icon = { 
                                    if (route == "notifications" && outOfBudget) {
                                        androidx.compose.material3.BadgedBox(
                                            badge = {
                                                androidx.compose.material3.Badge {
                                                    Text("!")
                                                }
                                            }
                                        ) {
                                            Icon(icon, contentDescription = null)
                                        }
                                    } else {
                                        Icon(icon, contentDescription = null)
                                    }
                                },
                                label = { Text(com.example.ui.trans(name), style = MaterialTheme.typography.labelSmall) },
                                selected = index == pagerState.currentPage,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color.Transparent
                                ),
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        com.example.ui.SplashScreen(
                            onFinish = {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToSignup = {
                                navController.navigate("signup")
                            }
                        )
                    }
                    composable("signup") {
                        SignupScreen(
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard") { 
                        LaunchedEffect(Unit) {
                            financeViewModel.refreshUser()
                        }
                        
                        val pagerScope = rememberCoroutineScope()
                        var lastBackPressTime by remember { mutableStateOf(0L) }
                        var showExitDialog by remember { mutableStateOf(false) }
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        
                        androidx.activity.compose.BackHandler(enabled = true) {
                            if (pagerState.currentPage > 0) {
                                pagerScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            } else {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastBackPressTime < 2000) {
                                    showExitDialog = true
                                } else {
                                    lastBackPressTime = currentTime
                                    android.widget.Toast.makeText(localContext, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        if (showExitDialog) {
                            AlertDialog(
                                onDismissRequest = { showExitDialog = false },
                                title = { Text(com.example.ui.trans("Exit")) },
                                text = { Text(com.example.ui.trans("are you want to exit?")) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showExitDialog = false
                                            navController.navigate("login") {
                                                popUpTo(0)
                                            }
                                        }
                                    ) { Text(com.example.ui.trans("Yes")) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExitDialog = false }) { Text(com.example.ui.trans("No")) }
                                }
                            )
                        }

                        androidx.compose.foundation.pager.HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> {
                                    DashboardScreen(
                                        viewModel = financeViewModel,
                                        onNavigateToAdd = { navController.navigate("add_transaction") },
                                        onNavigateToAnalytics = { navController.navigate("analytics") },
                                        onNavigateToPerDayRecord = { navController.navigate("per_day_record") }
                                    )
                                }
                                1 -> {
                                    TransactionHistoryScreen(viewModel = financeViewModel)
                                }
                                2 -> {
                                    NotificationsScreen(viewModel = financeViewModel)
                                }
                                3 -> {
                                    NotesScreen(onNavigateToCreateNode = { noteId -> 
                                        navController.navigate(if (noteId != null) "create_note?noteId=$noteId" else "create_note") 
                                    })
                                }
                            }
                        }
                    }
                    composable("per_day_record") { 
                        PerDayRecordScreen(
                            viewModel = financeViewModel,
                            onBack = { navController.popBackStack() },
                            onDayClick = { dateStr -> navController.navigate("daily_detail/$dateStr") }
                        ) 
                    }
                    composable(
                        route = "daily_detail/{dateStr}",
                        arguments = listOf(androidx.navigation.navArgument("dateStr") { type = androidx.navigation.NavType.StringType })
                    ) { backStackEntry ->
                        val dateStr = backStackEntry.arguments?.getString("dateStr") ?: ""
                        DailyDetailScreen(
                            viewModel = financeViewModel,
                            dateStr = dateStr,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = "create_note?noteId={noteId}",
                        arguments = listOf(androidx.navigation.navArgument("noteId") { type = androidx.navigation.NavType.IntType; defaultValue = -1 })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId").takeIf { it != -1 }
                        CreateNoteScreen(noteId = noteId, onBack = { navController.popBackStack() }) 
                    }
                    composable("add_transaction") { 
                        AddTransactionScreen(
                            viewModel = financeViewModel,
                            onBack = { navController.popBackStack() }
                        ) 
                    }
                    composable("analytics") { 
                        AnalyticsScreen(
                            viewModel = financeViewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToChart = { chartType -> navController.navigate("chart_detail/$chartType") }
                        ) 
                    }
                    composable("chart_detail/{chartType}") { backStackEntry ->
                        val chartType = backStackEntry.arguments?.getString("chartType") ?: "pie"
                        ChartDetailScreen(
                            chartType = chartType,
                            viewModel = financeViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("about") {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }
                    composable("bin") {
                        com.example.ui.BinScreen(viewModel = financeViewModel, onBack = { navController.popBackStack() })
                    }
                    composable("account_settings") { 
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        AccountSettingsScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                val prefs = localContext.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                                prefs.edit().remove("current_user").apply()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onThemeToggle = onThemeToggle
                        ) 
                    }
                }
            }
        }
    }
}
