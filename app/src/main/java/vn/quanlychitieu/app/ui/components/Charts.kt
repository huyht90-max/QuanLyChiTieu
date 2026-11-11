package vn.quanlychitieu.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vn.quanlychitieu.app.ui.Fmt

data class ChartSlice(val label: String, val value: Long, val color: Color)

/** Biểu đồ vành khuyên có hiệu ứng vẽ dần. */
@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    thickness: Dp = 26.dp,
    center: @Composable () -> Unit = {},
) {
    val total = slices.sumOf { it.value }.coerceAtLeast(1)
    val progress = remember(slices) { Animatable(0f) }
    LaunchedEffect(slices) {
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = thickness.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            if (slices.isEmpty()) return@Canvas
            val gap = if (slices.size > 1) 2.5f else 0f
            var start = -90f
            val sweepTotal = 360f * progress.value
            slices.forEach { s ->
                val sweep = 360f * s.value / total
                val visible = (sweepTotal - (start + 90f)).coerceIn(0f, sweep)
                if (visible > gap) {
                    drawArc(
                        color = s.color,
                        startAngle = start + gap / 2,
                        sweepAngle = visible - gap,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Butt),
                    )
                }
                start += sweep
            }
        }
        center()
    }
}

data class BarGroup(val label: String, val income: Long, val expense: Long)

/** Biểu đồ cột đôi Thu/Chi theo tháng. */
@Composable
fun IncomeExpenseBars(
    groups: List<BarGroup>,
    incomeColor: Color,
    expenseColor: Color,
    modifier: Modifier = Modifier,
    highlightLast: Boolean = true,
) {
    val max = groups.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1) ?: 1
    val progress = remember(groups) { Animatable(0f) }
    LaunchedEffect(groups) { progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }
    val grid = MaterialTheme.colorScheme.outlineVariant

    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            groups.forEachIndexed { i, g ->
                val alpha = if (!highlightLast || i == groups.lastIndex) 1f else 0.55f
                Row(
                    Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Bar(g.income.toFloat() / max * progress.value, incomeColor.copy(alpha = alpha))
                    Bar(g.expense.toFloat() / max * progress.value, expenseColor.copy(alpha = alpha))
                }
            }
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
        ) { drawLine(grid, Offset(0f, 0f), Offset(size.width, 0f), 2f) }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            groups.forEachIndexed { i, g ->
                Text(
                    g.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == groups.lastIndex) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(40.dp),
                )
            }
        }
    }
}

@Composable
private fun Bar(fraction: Float, color: Color) {
    Box(
        Modifier
            .width(12.dp)
            .fillMaxHeight(fraction.coerceIn(0.015f, 1f))
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
            .background(color)
    )
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Biểu đồ đường nhỏ chi tiêu theo ngày trong tháng. */
@Composable
fun DailySpark(values: List<Long>, color: Color, modifier: Modifier = Modifier) {
    val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val progress = remember(values) { Animatable(0f) }
    LaunchedEffect(values) { progress.animateTo(1f, tween(700)) }
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEach { v ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight((v.toFloat() / max * progress.value).coerceIn(0.04f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (v > 0) color else color.copy(alpha = 0.15f))
            )
        }
    }
}

fun compactLabel(v: Long) = Fmt.compact(v)
