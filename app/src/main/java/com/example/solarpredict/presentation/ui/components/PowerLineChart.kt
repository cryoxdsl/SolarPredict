package com.example.solarpredict.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun PowerLineChart(
    values: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        if (values.isEmpty()) return@Canvas
        val max = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
        val stepX = if (values.size > 1) size.width / (values.size - 1) else size.width
        val points = values.mapIndexed { i, v ->
            val x = i * stepX
            val y = size.height - ((v / max).toFloat() * size.height)
            Offset(x, y)
        }
        for (i in 1 until points.size) {
            drawLine(
                color = color,
                start = points[i - 1],
                end = points[i],
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
        drawLine(Color.LightGray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1f)
    }
}
