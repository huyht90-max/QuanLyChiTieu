package vn.quanlychitieu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.quanlychitieu.app.data.AppSettings
import vn.quanlychitieu.app.data.Categories
import vn.quanlychitieu.app.data.Transaction
import vn.quanlychitieu.app.data.TxType
import vn.quanlychitieu.app.ui.Fmt
import vn.quanlychitieu.app.ui.components.AppCard
import vn.quanlychitieu.app.ui.components.CategoryIcon
import vn.quanlychitieu.app.ui.components.DailySpark
import vn.quanlychitieu.app.ui.components.EmptyState
import vn.quanlychitieu.app.ui.components.ProgressBar
import vn.quanlychitieu.app.ui.components.SectionHeader
import vn.quanlychitieu.app.ui.components.TransactionRow
import vn.quanlychitieu.app.ui.inMonth
import vn.quanlychitieu.app.ui.summary
import vn.quanlychitieu.app.ui.theme.AmountStyle
import vn.quanlychitieu.app.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Composable
fun HomeScreen(
    transactions: List<Transaction>,
    settings: AppSettings,
    contentPadding: PaddingValues,
    onToggleHide: () -> Unit,
    onOpen: (Long) -> Unit,
    onSeeAll: () -> Unit,
    onSetBudget: () -> Unit,
    onOpenStats: () -> Unit,
) {
    val month = YearMonth.now()
    val hidden = settings.hideAmounts
    val total = remember(transactions) { transactions.summary() }
    val monthTx = remember(transactions) { transactions.inMonth(month) }
    val monthSum = remember(monthTx) { monthTx.summary() }
    val topCats = remember(monthTx) {
        monthTx.filter { it.type == TxType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (id, list) -> id to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
            .take(4)
    }
    val daily = remember(monthTx) {
        val days = LocalDate.now().dayOfMonth
        val arr = LongArray(days)
        monthTx.filter { it.type == TxType.EXPENSE }.forEach {
            val d = Fmt.date(it.timestamp).dayOfMonth
            if (d in 1..days) arr[d - 1] += it.amount
        }
        arr.toList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = 0.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(settings.userName, hidden, onToggleHide)
        }
        item {
            BalanceCard(
                balance = total.balance,
                income = monthSum.income,
                expense = monthSum.expense,
                hidden = hidden,
            )
        }
        item {
            BudgetCard(
                budget = settings.monthlyBudget,
                spent = monthSum.expense,
                hidden = hidden,
                onClick = onSetBudget,
            )
        }
        if (monthSum.expense > 0) {
            item {
                AppCard(onClick = onOpenStats) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Chi tiêu theo ngày", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Trung bình ${Fmt.money(monthSum.expense / LocalDate.now().dayOfMonth, hidden)}/ngày",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    DailySpark(daily, MaterialTheme.colorScheme.primary)
                    if (topCats.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        topCats.forEach { (id, amount) ->
                            val cat = Categories.get(id)
                            Row(
                                Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CategoryIcon(cat, size = 34.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row {
                                        Text(cat.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        Text(
                                            Fmt.money(amount, hidden),
                                            style = MaterialTheme.typography.bodyMedium.merge(AmountStyle),
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    ProgressBar(amount.toFloat() / monthSum.expense, cat.color, height = 6.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionHeader("Giao dịch gần đây", if (transactions.isNotEmpty()) "Xem tất cả" else null, onSeeAll)
        }
        if (transactions.isEmpty()) {
            item {
                AppCard {
                    EmptyState(
                        Icons.AutoMirrored.Rounded.ReceiptLong,
                        "Chưa có giao dịch",
                        "Nhấn nút + để ghi lại khoản thu chi đầu tiên của bạn",
                    )
                }
            }
        } else {
            item {
                AppCard(padding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)) {
                    transactions.take(6).forEach { tx ->
                        TransactionRow(tx, hidden, showDate = true) { onOpen(tx.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(name: String, hidden: Boolean, onToggleHide: () -> Unit) {
    val hour = LocalTime.now().hour
    val greeting = when (hour) {
        in 5..10 -> "Chào buổi sáng"
        in 11..13 -> "Chào buổi trưa"
        in 14..17 -> "Chào buổi chiều"
        else -> "Chào buổi tối"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(AppTheme.finance.heroGradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.trim().firstOrNull()?.uppercase() ?: "👋",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                greeting + if (name.isNotBlank()) "," else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                name.ifBlank { Fmt.fullDate(LocalDate.now()) },
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = onToggleHide,
            modifier = Modifier
                .clip(CircleShape)
                .background(AppTheme.finance.card),
        ) {
            Icon(
                if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                contentDescription = if (hidden) "Hiện số tiền" else "Ẩn số tiền",
            )
        }
    }
}

@Composable
private fun BalanceCard(balance: Long, income: Long, expense: Long, hidden: Boolean) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(AppTheme.finance.heroGradient)
    ) {
        // Vòng tròn trang trí
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-70).dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 50.dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Column(Modifier.padding(22.dp)) {
            Text("Tổng số dư", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                Fmt.money(balance, hidden),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                maxLines = 1,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroStat("Thu tháng này", income, Icons.Rounded.ArrowDownward, hidden, Modifier.weight(1f))
                HeroStat("Chi tháng này", expense, Icons.Rounded.ArrowUpward, hidden, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hidden: Boolean,
    modifier: Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
            Text(
                if (hidden) "••••" else Fmt.compact(value),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.merge(AmountStyle),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BudgetCard(budget: Long, spent: Long, hidden: Boolean, onClick: () -> Unit) {
    val fin = AppTheme.finance
    AppCard(onClick = onClick) {
        if (budget <= 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Savings, null, tint = MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Đặt ngân sách tháng", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Kiểm soát chi tiêu tốt hơn với hạn mức mỗi tháng",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@AppCard
        }
        val ratio = spent.toFloat() / budget
        val remain = budget - spent
        val daysLeft = YearMonth.now().lengthOfMonth() - LocalDate.now().dayOfMonth + 1
        val color = when {
            ratio >= 1f -> fin.expense
            ratio >= 0.8f -> Color(0xFFF59E0B)
            else -> fin.income
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Ngân sách tháng", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Đã chi ${Fmt.money(spent, hidden)} / ${Fmt.money(budget, hidden)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "${(ratio * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = color,
            )
        }
        Spacer(Modifier.height(12.dp))
        ProgressBar(ratio, color, height = 10.dp)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (remain < 0) {
                Icon(Icons.Rounded.WarningAmber, null, tint = fin.expense, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Vượt ngân sách ${Fmt.money(-remain, hidden)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = fin.expense,
                )
            } else {
                Text(
                    "Còn lại ${Fmt.money(remain, hidden)} · ~${Fmt.money(remain / daysLeft, hidden)}/ngày",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
