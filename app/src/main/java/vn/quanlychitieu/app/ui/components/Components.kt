package vn.quanlychitieu.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vn.quanlychitieu.app.data.Categories
import vn.quanlychitieu.app.data.Category
import vn.quanlychitieu.app.data.Transaction
import vn.quanlychitieu.app.data.TxType
import vn.quanlychitieu.app.ui.Fmt
import vn.quanlychitieu.app.ui.theme.AmountStyle
import vn.quanlychitieu.app.ui.theme.AppTheme
import java.time.YearMonth

/** Thẻ nền trắng bo góc dùng xuyên suốt ứng dụng. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = AppTheme.finance.card,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}

@Composable
fun CategoryIcon(
    category: Category,
    size: Dp = 44.dp,
    selected: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(if (selected) category.color else category.color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            category.icon,
            contentDescription = category.name,
            tint = if (selected) Color.White else category.color,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun TransactionRow(
    tx: Transaction,
    hidden: Boolean,
    showDate: Boolean = false,
    onClick: () -> Unit,
) {
    val cat = Categories.get(tx.categoryId)
    val fin = AppTheme.finance
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(cat)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                cat.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
            )
            val sub = buildString {
                if (tx.note.isNotBlank()) append(tx.note) else append(if (tx.type == TxType.INCOME) "Thu nhập" else "Chi tiêu")
            }
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                Fmt.signed(tx.amount, tx.type, hidden),
                style = MaterialTheme.typography.titleSmall.merge(AmountStyle),
                color = if (tx.type == TxType.INCOME) fin.income else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                if (showDate) Fmt.shortDate(Fmt.date(tx.timestamp)) else Fmt.time(tx.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun MonthSwitcher(
    month: YearMonth,
    onChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canNext = month < YearMonth.now()
    Row(
        modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onChange(month.minusMonths(1)) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Tháng trước")
        }
        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * dir / 2 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it * dir / 2 } + fadeOut())
            },
            modifier = Modifier.weight(1f),
            label = "month",
        ) { m ->
            Text(
                Fmt.monthTitle(m) + if (m == YearMonth.now() || m == YearMonth.now().minusMonths(1)) " · ${m.monthValue}/${m.year}" else "",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        IconButton(
            onClick = { onChange(month.plusMonths(1)) },
            enabled = canNext,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Tháng sau")
        }
    }
}

@Composable
fun ProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val animated by animateFloatAsStateCompat(fraction.coerceIn(0f, 1f))
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun animateFloatAsStateCompat(target: Float) =
    animateFloatAsState(target, animationSpec = tween(700), label = "progress")

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Ô thống kê nhỏ có icon tròn (Thu / Chi). */
@Composable
fun StatPill(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    container: Color,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier, padding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(container),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value,
                    style = MaterialTheme.typography.titleSmall.merge(AmountStyle),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
