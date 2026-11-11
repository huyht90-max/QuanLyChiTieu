package vn.quanlychitieu.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vn.quanlychitieu.app.data.AppSettings
import vn.quanlychitieu.app.data.Categories
import vn.quanlychitieu.app.data.Transaction
import vn.quanlychitieu.app.data.TxType
import vn.quanlychitieu.app.ui.Fmt
import vn.quanlychitieu.app.ui.components.AppCard
import vn.quanlychitieu.app.ui.components.BarGroup
import vn.quanlychitieu.app.ui.components.CategoryIcon
import vn.quanlychitieu.app.ui.components.ChartSlice
import vn.quanlychitieu.app.ui.components.DonutChart
import vn.quanlychitieu.app.ui.components.EmptyState
import vn.quanlychitieu.app.ui.components.IncomeExpenseBars
import vn.quanlychitieu.app.ui.components.LegendDot
import vn.quanlychitieu.app.ui.components.MonthSwitcher
import vn.quanlychitieu.app.ui.components.ProgressBar
import vn.quanlychitieu.app.ui.inMonth
import vn.quanlychitieu.app.ui.summary
import vn.quanlychitieu.app.ui.theme.AmountStyle
import vn.quanlychitieu.app.ui.theme.AppTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun StatsScreen(
    transactions: List<Transaction>,
    settings: AppSettings,
    contentPadding: PaddingValues,
) {
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var type by rememberSaveable { mutableStateOf(TxType.EXPENSE) }
    val hidden = settings.hideAmounts
    val fin = AppTheme.finance

    val monthTx = remember(transactions, month) { transactions.inMonth(month) }
    val sum = remember(monthTx) { monthTx.summary() }
    val byCat = remember(monthTx, type) {
        monthTx.filter { it.type == type }
            .groupBy { it.categoryId }
            .map { (id, l) -> Triple(Categories.get(id), l.sumOf { it.amount }, l.size) }
            .sortedByDescending { it.second }
    }
    val typeTotal = byCat.sumOf { it.second }
    val slices = remember(byCat) { byCat.map { ChartSlice(it.first.name, it.second, it.first.color) } }
    val bars = remember(transactions, month) {
        (5 downTo 0).map { back ->
            val ym = month.minusMonths(back.toLong())
            val s = transactions.inMonth(ym).summary()
            BarGroup("T${ym.monthValue}", s.income, s.expense)
        }
    }
    val days = if (month == YearMonth.now()) LocalDate.now().dayOfMonth else month.lengthOfMonth()
    val prevExpense = remember(transactions, month) { transactions.inMonth(month.minusMonths(1)).summary().expense }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "Thống kê",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 18.dp, start = 4.dp),
            )
        }
        item { MonthSwitcher(month, { month = it }, Modifier.fillMaxWidth()) }
        item {
            TypeToggle(type) { type = it }
        }
        item {
            AppCard {
                if (byCat.isEmpty()) {
                    EmptyState(
                        Icons.Rounded.PieChart,
                        "Chưa có dữ liệu",
                        "Không có khoản ${if (type == TxType.EXPENSE) "chi" else "thu"} nào trong tháng này",
                    )
                } else {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        DonutChart(slices, size = 210.dp) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (type == TxType.EXPENSE) "Tổng chi" else "Tổng thu",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    if (hidden) "••••" else Fmt.compact(typeTotal),
                                    style = MaterialTheme.typography.headlineSmall.merge(AmountStyle),
                                    color = if (type == TxType.EXPENSE) fin.expense else fin.income,
                                )
                                Text(
                                    "${byCat.sumOf { it.third }} giao dịch",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    byCat.forEach { (cat, amount, count) ->
                        val pct = amount.toFloat() / typeTotal
                        Row(
                            Modifier.padding(vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryIcon(cat, size = 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(cat.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "  ·  $count lần",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        Fmt.money(amount, hidden),
                                        style = MaterialTheme.typography.titleSmall.merge(AmountStyle),
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProgressBar(pct, cat.color, Modifier.weight(1f), height = 6.dp)
                                    Text(
                                        "${String.format(java.util.Locale.forLanguageTag("vi"), "%.1f", pct * 100)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(48.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightCard(
                    "Chi trung bình/ngày",
                    Fmt.money(if (days > 0) sum.expense / days else 0, hidden),
                    Modifier.weight(1f),
                )
                val diff = sum.expense - prevExpense
                val pct = if (prevExpense > 0) diff * 100 / prevExpense else null
                InsightCard(
                    "So với tháng trước",
                    when {
                        pct == null -> "—"
                        pct > 0 -> "▲ $pct%"
                        pct < 0 -> "▼ ${-pct}%"
                        else -> "Không đổi"
                    },
                    Modifier.weight(1f),
                    valueColor = when {
                        pct == null || pct == 0L -> MaterialTheme.colorScheme.onSurface
                        pct > 0 -> fin.expense
                        else -> fin.income
                    },
                )
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("6 tháng gần nhất", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    LegendDot(fin.income, "Thu")
                    Spacer(Modifier.width(12.dp))
                    LegendDot(fin.expense, "Chi")
                }
                Spacer(Modifier.height(16.dp))
                IncomeExpenseBars(bars, fin.income, fin.expense)
                Spacer(Modifier.height(12.dp))
                val s = bars.last()
                Text(
                    "Tháng ${month.monthValue}: thu ${Fmt.money(s.income, hidden)}, chi ${Fmt.money(s.expense, hidden)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InsightCard(label: String, value: String, modifier: Modifier, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    AppCard(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium.merge(AmountStyle), color = valueColor, maxLines = 1)
    }
}

@Composable
fun TypeToggle(type: TxType, onChange: (TxType) -> Unit) {
    val fin = AppTheme.finance
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
    ) {
        listOf(TxType.EXPENSE to "Chi tiêu", TxType.INCOME to "Thu nhập").forEach { (t, label) ->
            val selected = t == type
            val activeColor = if (t == TxType.EXPENSE) fin.expense else fin.income
            val bg by animateColorAsState(if (selected) activeColor else Color.Transparent, label = "bg")
            val fg by animateColorAsState(
                if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, label = "fg",
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .clickable { onChange(t) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
