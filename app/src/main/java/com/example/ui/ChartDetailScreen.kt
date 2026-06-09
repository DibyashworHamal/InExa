package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDetailScreen(chartType: String, viewModel: FinanceViewModel, onBack: () -> Unit) {
    val title = when (chartType) {
        "pie_chart" -> "Category Distribution"
        "bar_chart" -> "Monthly Comparison"
        "weekly_graph" -> "Weekly Expense"
        "income_vs_expense_graph" -> "Income vs Expense"
        else -> "Analytics"
    }

    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animate = true
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (animate) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
    )
    
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val totalIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle(initialValue = 0.0)
    val totalExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle(initialValue = 0.0)

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("user_credentials", android.content.Context.MODE_PRIVATE)
    val currencyCode = prefs.getString("currency", "NPR") ?: "NPR"
    val formatter = remember(currencyCode) {
        com.example.ui.CurrencyUtils.getFormatter(currencyCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (chartType) {
                "pie_chart" -> FullPieChart(getCategoryData(transactions), animationProgress, formatter, currencyCode, Modifier.fillMaxSize())
                "bar_chart" -> FullBarChart(getMonthlyData(transactions), animationProgress, formatter, currencyCode, Modifier.fillMaxSize())
                "weekly_graph" -> FullLineChart(getWeeklyData(transactions), animationProgress, formatter, currencyCode, Modifier.fillMaxSize())
                "income_vs_expense_graph" -> FullIncomeExpenseChart(totalIncome, totalExpense, animationProgress, formatter, currencyCode, Modifier.fillMaxSize())
                else -> Text("Unknown Chart Type")
            }
        }
    }
}

@Composable
fun FullPieChart(data: List<Pair<String, Double>>, progress: Float, formatter: java.text.NumberFormat, currencyCode: String, modifier: Modifier = Modifier) {
    if (data.isEmpty() || data.sumOf { it.second } == 0.0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Expense Data", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val strokeWidth = 80f
                val total = data.sumOf { it.second }.toFloat()
                var startAngle = -90f
                
                data.forEachIndexed { index, pair ->
                    val sweepAngle = (pair.second.toFloat() / total) * 360f * progress
                    val color = ChartColors[index % ChartColors.size]
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(data.size) { index ->
                val (category, amount) = data[index]
                val formattedAmount = formatter.format(com.example.ui.CurrencyUtils.convertFromNPR(amount, currencyCode))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(ChartColors[index % ChartColors.size], CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(category, style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(formattedAmount, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FullBarChart(data: List<Pair<String, Double>>, progress: Float, formatter: java.text.NumberFormat, currencyCode: String, modifier: Modifier = Modifier) {
    if (data.all { it.second == 0.0 }) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Expense Data", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    val maxVal = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)
    val barColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Row(modifier = Modifier.fillMaxWidth().height(300.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            data.forEach { (label, value) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val formattedAmount = formatter.format(com.example.ui.CurrencyUtils.convertFromNPR(value, currencyCode))
                    Text(if (value > 0.0) formattedAmount else "", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                    
                    val heightRatio = (value.toFloat() / maxVal).coerceAtLeast(0.01f) * progress
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .weight(1f, fill = false)
                            .fillMaxHeight(heightRatio)
                            .background(barColor, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun FullLineChart(data: List<Pair<String, Double>>, progress: Float, formatter: java.text.NumberFormat, currencyCode: String, modifier: Modifier = Modifier) {
    if (data.all { it.second == 0.0 }) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Expense Data", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    val maxVal = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)
    val lineColor = MaterialTheme.colorScheme.secondary

    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            val stepX = size.width / (data.size - 1).coerceAtLeast(1)
            val path = androidx.compose.ui.graphics.Path()
            
            val currentPointLimit = (data.size * progress).toInt().coerceAtLeast(1)
            
            for (index in 0 until minOf(currentPointLimit + 1, data.size)) {
                val heightRatio = (data[index].second.toFloat() / maxVal)
                val x = index * stepX
                val y = size.height - (size.height * heightRatio)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            
            drawPath(path, color = lineColor, style = Stroke(width = 12f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEach { (label, value) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    if (value > 0) {
                        val formattedAmount = formatter.format(com.example.ui.CurrencyUtils.convertFromNPR(value, currencyCode))
                        Text(formattedAmount, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun FullIncomeExpenseChart(income: Double, expense: Double, progress: Float, formatter: java.text.NumberFormat, currencyCode: String, modifier: Modifier = Modifier) {
    val total = (income + expense).takeIf { it > 0 } ?: 1.0
    val incRatio = (income / total).toFloat().coerceAtLeast(0.01f)
    val expRatio = (expense / total).toFloat().coerceAtLeast(0.01f)
    
    val incomeColor = Color(0xFF16A34A)
    val expenseColor = Color(0xFFDC2626)
    
    val incFormatted = formatter.format(com.example.ui.CurrencyUtils.convertFromNPR(income, currencyCode))
    val expFormatted = formatter.format(com.example.ui.CurrencyUtils.convertFromNPR(expense, currencyCode))

    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Row(modifier = Modifier.fillMaxWidth().height(300.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(incFormatted, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = incomeColor, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight(incRatio * 0.8f * progress)
                        .background(incomeColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Income", style = MaterialTheme.typography.titleMedium)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(expFormatted, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = expenseColor, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight(expRatio * 0.8f * progress)
                        .background(expenseColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Expense", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
