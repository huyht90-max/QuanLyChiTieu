package vn.quanlychitieu.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.unit.dp
import vn.quanlychitieu.app.data.AppSettings
import vn.quanlychitieu.app.data.Categories
import vn.quanlychitieu.app.data.Transaction
import vn.quanlychitieu.app.data.TxType
import vn.quanlychitieu.app.ui.Fmt
import vn.quanlychitieu.app.ui.components.AppCard
import vn.quanlychitieu.app.ui.components.EmptyState
import vn.quanlychitieu.app.ui.components.MonthSwitcher
import vn.quanlychitieu.app.ui.components.TransactionRow
import vn.quanlychitieu.app.ui.inMonth
import vn.quanlychitieu.app.ui.summary
import vn.quanlychitieu.app.ui.theme.AmountStyle
import vn.quanlychitieu.app.ui.theme.AppTheme
import java.text.Normalizer
import java.time.YearMonth

private enum class Filter(val label: String) { ALL("Tất cả"), EXPENSE("Chi tiêu"), INCOME("Thu nhập") }

/** Bỏ dấu tiếng Việt để tìm kiếm "an uong" vẫn ra "Ăn uống". */
private fun String.fold(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .replace('đ', 'd')

@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    settings: AppSettings,
    contentPadding: PaddingValues,
    onOpen: (Long) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var filter by rememberSaveable { mutableStateOf(Filter.ALL) }
    var query by rememberSaveable { mutableStateOf("") }
    var searching by rememberSaveable { mutableStateOf(false) }
    val hidden = settings.hideAmounts
    val fin = AppTheme.finance

    val monthTx = remember(transactions, month) { transactions.inMonth(month) }
    val sum = remember(monthTx) { monthTx.summary() }
    val shown = remember(monthTx, filter, query) {
        val q = query.trim().fold()
        monthTx.filter {
            when (filter) {
                Filter.ALL -> true
                Filter.EXPENSE -> it.type == TxType.EXPENSE
                Filter.INCOME -> it.type == TxType.INCOME
            }
        }.filter {
            q.isEmpty() || it.note.fold().contains(q) || Categories.get(it.categoryId).name.fold().contains(q) ||
                it.amount.toString().contains(q.filter(Char::isDigit).ifEmpty { "\u0000" })
        }
    }
    val groups = remember(shown) { shown.groupBy { Fmt.date(it.timestamp) }.toList() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Sổ giao dịch", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    searching = !searching
                    if (!searching) query = ""
                }) {
                    Icon(if (searching) Icons.Rounded.Close else Icons.Rounded.Search, "Tìm kiếm")
                }
            }
        }
        item {
            AnimatedVisibility(searching) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Tìm theo ghi chú, danh mục, số tiền…") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = fin.card,
                        unfocusedContainerColor = fin.card,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item { MonthSwitcher(month, { month = it }, Modifier.fillMaxWidth()) }
        item {
            AppCard {
                Row {
                    SummaryCell("Thu nhập", Fmt.money(sum.income, hidden), fin.income, Modifier.weight(1f))
                    SummaryCell("Chi tiêu", Fmt.money(sum.expense, hidden), fin.expense, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Chênh lệch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        (if (sum.balance >= 0) "+" else "−") + Fmt.money(kotlin.math.abs(sum.balance), hidden),
                        style = MaterialTheme.typography.titleMedium.merge(AmountStyle),
                        color = if (sum.balance >= 0) fin.income else fin.expense,
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Filter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = fin.card,
                        ),
                        border = null,
                    )
                }
            }
        }

        if (groups.isEmpty()) {
            item {
                EmptyState(
                    if (query.isNotBlank()) Icons.Rounded.SearchOff else Icons.Rounded.Search,
                    if (query.isNotBlank()) "Không tìm thấy kết quả" else "Không có giao dịch",
                    if (query.isNotBlank()) "Thử từ khóa khác nhé" else "Chưa có khoản thu chi nào trong ${Fmt.monthTitle(month).lowercase()}",
                )
            }
        }

        groups.forEach { (day, list) ->
            item(key = "h$day") {
                val daySum = list.summary()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        Fmt.dayHeader(day),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        (if (daySum.balance >= 0) "+" else "−") + Fmt.money(kotlin.math.abs(daySum.balance), hidden),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item(key = "g$day") {
                AppCard(padding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)) {
                    list.forEach { tx ->
                        SwipeRow(tx, hidden, onOpen = { onOpen(tx.id) }, onDelete = { onDelete(tx) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCell(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium.merge(AmountStyle), color = color, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeRow(tx: Transaction, hidden: Boolean, onOpen: () -> Unit, onDelete: () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); true
            } else false
        },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.finance.expense)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Xóa", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.padding(4.dp))
                    Icon(Icons.Rounded.DeleteOutline, null, tint = Color.White)
                }
            }
        },
    ) {
        Box(Modifier.background(AppTheme.finance.card)) {
            TransactionRow(tx, hidden, onClick = onOpen)
        }
    }
}
