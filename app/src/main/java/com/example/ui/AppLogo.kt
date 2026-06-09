package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    val greenColor = Color(0xFF22C55E)
    val redColor = Color(0xFFEF4444)
    val baseColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val annotatedString = buildAnnotatedString {
            pushStyle(SpanStyle(color = greenColor))
            append("In")
            pop()
            pushStyle(SpanStyle(color = redColor))
            append("Ex")
            pop()
            pushStyle(SpanStyle(color = baseColor))
            append("a")
            pop()
        }

        Text(
            text = annotatedString,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Large curved bottom smile curved line (green-red gradient trend line representing smiley/growth)
        Canvas(modifier = Modifier.width(90.dp).height(12.dp)) {
            val totalWidth = size.width
            val curveY = 2f
            val smileHeight = size.height

            val pathGreen = Path().apply {
                moveTo(0f, curveY)
                quadraticBezierTo(
                    totalWidth / 4f, smileHeight,
                    totalWidth / 2f + 1f, smileHeight
                )
            }

            val pathRed = Path().apply {
                moveTo(totalWidth / 2f - 1f, smileHeight)
                quadraticBezierTo(
                    totalWidth * 3f / 4f, smileHeight,
                    totalWidth, curveY
                )
            }

            drawPath(
                path = pathGreen,
                color = greenColor,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = pathRed,
                color = redColor,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun DesignedText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
) {
    val greenColor = Color(0xFF22C55E)
    val redColor = Color(0xFFEF4444)
    val baseColor = MaterialTheme.colorScheme.onSurface

    val annotatedString = buildAnnotatedString {
        val len = text.length
        if (len == 0) {
            // empty
        } else if (text == "InExa") {
            pushStyle(SpanStyle(color = greenColor))
            append("In")
            pop()
            pushStyle(SpanStyle(color = redColor))
            append("Ex")
            pop()
            pushStyle(SpanStyle(color = baseColor))
            append("a")
            pop()
        } else {
            var p1Ratio = 0.33f
            var p2Ratio = 0.33f
            
            val clean = text.trim()
            if (clean.startsWith("In", ignoreCase = true) && clean.contains("Ex", ignoreCase = true)) {
                p1Ratio = 0.40f
                p2Ratio = 0.40f
            } else if (clean.equals("History", ignoreCase = true) || clean.equals("इतिहास", ignoreCase = true) || clean.equals("Historial", ignoreCase = true) || clean.equals("Historique", ignoreCase = true)) {
                p1Ratio = 0.42f
                p2Ratio = 0.28f
            } else if (clean.equals("Notifications", ignoreCase = true) || clean.equals("सूचनाहरू", ignoreCase = true) || clean.equals("Notificaciones", ignoreCase = true) || clean.equals("Notifications", ignoreCase = true) || clean.equals("सूचनाएं", ignoreCase = true)) {
                p1Ratio = 0.31f
                p2Ratio = 0.31f
            } else if (clean.equals("Keep Notes", ignoreCase = true) || clean.equals("नोटहरू राख्नुहोस्", ignoreCase = true) || clean.equals("Guardar Notas", ignoreCase = true) || clean.equals("Conserver les Notes", ignoreCase = true) || clean.equals("नोट्स रखें", ignoreCase = true)) {
                p1Ratio = 0.40f
                p2Ratio = 0.30f
            }

            val part1Len = (len * p1Ratio).toInt().coerceAtLeast(1).coerceAtMost(len)
            val part2Len = (len * p2Ratio).toInt().coerceAtLeast(0).coerceAtMost(len - part1Len)
            
            val p1 = text.substring(0, part1Len)
            val p2 = text.substring(part1Len, part1Len + part2Len)
            val p3 = text.substring(part1Len + part2Len)

            if (p1.isNotEmpty()) {
                pushStyle(SpanStyle(color = greenColor))
                append(p1)
                pop()
            }
            if (p2.isNotEmpty()) {
                pushStyle(SpanStyle(color = redColor))
                append(p2)
                pop()
            }
            if (p3.isNotEmpty()) {
                pushStyle(SpanStyle(color = baseColor))
                append(p3)
                pop()
            }
        }
    }

    Text(
        text = annotatedString,
        style = style,
        modifier = modifier
    )
}
