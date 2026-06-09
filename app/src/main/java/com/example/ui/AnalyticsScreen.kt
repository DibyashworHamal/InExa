package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

fun getCategoryData(transactions: List<Transaction>): List<Pair<String, Double>> {
    return transactions.filter { it.type == "EXPENSE" }
        .groupBy { it.categoryName }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList().sortedByDescending { it.second }
}

fun getMonthlyData(transactions: List<Transaction>): List<Pair<String, Double>> {
    val formatter = SimpleDateFormat("MMM", Locale.getDefault())
    val cal = Calendar.getInstance()
    val last6Months = mutableListOf<Pair<String, Double>>()
    
    val grouped = transactions.filter { it.type == "EXPENSE" }
        .groupBy { formatter.format(Date(it.dateMillis)) }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    for (i in 5 downTo 0) {
        val monthCal = Calendar.getInstance()
        monthCal.add(Calendar.MONTH, -i)
        val monthName = formatter.format(monthCal.time)
        last6Months.add(monthName to (grouped[monthName] ?: 0.0))
    }
    return last6Months
}

fun getWeeklyData(transactions: List<Transaction>): List<Pair<String, Double>> {
    val formatter = SimpleDateFormat("EEE", Locale.getDefault())
    val map = transactions.filter { it.type == "EXPENSE" }
        .groupBy { formatter.format(Date(it.dateMillis)) }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        
    val last7Days = mutableListOf<Pair<String, Double>>()
    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val dayString = formatter.format(cal.time)
        last7Days.add(dayString to (map[dayString] ?: 0.0))
    }
    return last7Days
}

val ChartColors = listOf(
    Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B), 
    Color(0xFFEF4444), Color(0xFF8B5CF6), Color(0xFFEC4899)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: FinanceViewModel, onBack: () -> Unit, onNavigateToChart: (String) -> Unit = {}) {
    val totalIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle(initialValue = 0.0)
    val totalExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle(initialValue = 0.0)
    
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val catData = getCategoryData(transactions)
    val monthlyData = getMonthlyData(transactions)
    val weeklyData = getWeeklyData(transactions)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                AnalyticsCard(title = "Category Pie Chart", subtitle = "Distribution of your expenses", onClick = { onNavigateToChart("pie_chart") }) {
                    MiniPieChart(catData, modifier = Modifier.fillMaxWidth().height(140.dp))
                }
            }

            item {
                AnalyticsCard(title = "Monthly Bar Chart", subtitle = "Expenses over last 6 months", onClick = { onNavigateToChart("bar_chart") }) {
                    MiniBarChart(monthlyData, modifier = Modifier.fillMaxWidth().height(140.dp))
                }
            }

            item {
                AnalyticsCard(title = "Weekly Expense", subtitle = "Trend over the last 7 days", onClick = { onNavigateToChart("weekly_graph") }) {
                    MiniLineChart(weeklyData, modifier = Modifier.fillMaxWidth().height(140.dp))
                }
            }

            item {
                AnalyticsCard(title = "Income vs Expense", subtitle = "Total cash flow comparison", onClick = { onNavigateToChart("income_vs_expense_graph") }) {
                    MiniIncomeExpenseChart(totalIncome, totalExpense, modifier = Modifier.fillMaxWidth().height(140.dp))
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, subtitle: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun MiniPieChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    if (data.isEmpty() || data.sumOf { it.second } == 0.0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Expense Data", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val total = data.sumOf { it.second }.toFloat()
            var startAngle = -90f
            val strokeWidth = 24f
            
            data.forEachIndexed { index, pair ->
                val sweepAngle = (pair.second.toFloat() / total) * 360f
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
        
        Spacer(modifier = Modifier.width(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            data.take(4).forEachIndexed { index, pair ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(ChartColors[index % ChartColors.size], CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(pair.first, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun MiniBarChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    if (data.all { it.second == 0.0 }) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Expense Data", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val maxVal = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)
    val barColor = MaterialTheme.colorScheme.primary

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        data.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                val heightRatio = (value.toFloat() / maxVal).coerceAtLeast(0.01f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .weight(1f, fill = false)
                        .fillMaxHeight(heightRatio)
                        .background(barColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun MiniLineChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    if (data.all { it.second == 0.0 }) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Expense Data", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val maxVal = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)
    val lineColor = MaterialTheme.colorScheme.secondary

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val stepX = size.width / (data.size - 1).coerceAtLeast(1)
            val path = androidx.compose.ui.graphics.Path()
            
            data.forEachIndexed { index, (_, value) ->
                val heightRatio = (value.toFloat() / maxVal)
                val x = index * stepX
                val y = size.height - (size.height * heightRatio)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = lineColor, style = Stroke(width = 6f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEach { (label, _) ->
                Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun MiniIncomeExpenseChart(income: Double, expense: Double, modifier: Modifier = Modifier) {
    val total = (income + expense).takeIf { it > 0 } ?: 1.0
    val incRatio = (income / total).toFloat().coerceAtLeast(0.01f)
    val expRatio = (expense / total).toFloat().coerceAtLeast(0.01f)
    
    val incomeColor = Color(0xFF16A34A)
    val expenseColor = Color(0xFFDC2626)

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight(incRatio * 0.8f) // *0.8 to leave room for labels
                    .background(incomeColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Income", style = MaterialTheme.typography.labelSmall)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight(expRatio * 0.8f)
                    .background(expenseColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Expense", style = MaterialTheme.typography.labelSmall)
        }
    }
}
