package vn.quanlychitieu.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.quanlychitieu.app.data.Categories
import vn.quanlychitieu.app.data.Transaction
import vn.quanlychitieu.app.data.TxType
import vn.quanlychitieu.app.ui.Fmt
import vn.quanlychitieu.app.ui.components.CategoryIcon
import vn.quanlychitieu.app.ui.theme.AppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTransactionScreen(
    existing: Transaction?,
    initialType: TxType,
    onSave: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onBack: () -> Unit,
) {
    val fin = AppTheme.finance
    var type by rememberSaveable { mutableStateOf(existing?.type ?: initialType) }
    var digits by rememberSaveable { mutableStateOf(existing?.amount?.toString() ?: "") }
    var categoryId by rememberSaveable {
        mutableStateOf(existing?.categoryId ?: Categories.of(initialType).first().id)
    }
    var note by rememberSaveable { mutableStateOf(existing?.note ?: "") }
    var date by rememberSaveable {
        mutableStateOf(existing?.let { Fmt.date(it.timestamp) } ?: LocalDate.now())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val focus = remember { FocusRequester() }

    val accent by animateColorAsState(if (type == TxType.EXPENSE) fin.expense else fin.income, label = "accent")
    val amount = digits.toLongOrNull() ?: 0L

    LaunchedEffect(Unit) { if (existing == null) runCatching { focus.requestFocus() } }

    fun switchType(t: TxType) {
        if (t == type) return
        type = t
        if (Categories.get(categoryId).type != t) categoryId = Categories.of(t).first().id
    }

    fun submit() {
        if (amount <= 0) {
            error = "Vui lòng nhập số tiền"
            return
        }
        val time = existing?.let {
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalTime()
        } ?: LocalTime.now()
        val ts = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        onSave(
            Transaction(
                id = existing?.id ?: 0,
                type = type,
                amount = amount,
                categoryId = categoryId,
                note = note.trim(),
                timestamp = ts,
            )
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Thanh tiêu đề
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.Close, "Đóng") }
            Text(
                if (existing == null) "Thêm giao dịch" else "Sửa giao dịch",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            if (existing != null) {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Rounded.DeleteOutline, "Xóa", tint = fin.expense)
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            TypeToggle(type, ::switchType)
            Spacer(Modifier.height(28.dp))

            // Ô nhập số tiền lớn
            Text(
                "Số tiền",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            val display = if (digits.isEmpty()) "" else Fmt.number(amount)
            val amountStyle = TextStyle(
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp,
            )
            val measurer = rememberTextMeasurer()
            val density = LocalDensity.current
            // Ô nhập co giãn đúng theo độ rộng chữ để ký hiệu ₫ luôn nằm sát số
            val fieldWidth = with(density) {
                measurer.measure(display.ifEmpty { "0" }, amountStyle, maxLines = 1).size.width.toDp() + 6.dp
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = TextFieldValue(display, TextRange(display.length)),
                    onValueChange = { v ->
                        digits = v.text.filter(Char::isDigit).trimStart('0').take(13)
                        error = null
                    },
                    textStyle = amountStyle,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .width(fieldWidth)
                        .focusRequester(focus),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.Center) {
                            if (display.isEmpty()) {
                                Text(
                                    "0",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accent.copy(alpha = 0.35f),
                                )
                            }
                            inner()
                        }
                    },
                )
                Text(" ₫", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = accent)
            }
            if (error != null) {
                Text(
                    error!!,
                    color = fin.expense,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(12.dp))
            // Gợi ý số tiền nhanh
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                listOf(10_000L, 50_000L, 100_000L, 500_000L).forEach { v ->
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppTheme.finance.card)
                            .clickable {
                                digits = (amount + v).toString()
                                error = null
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text("+${Fmt.compact(v)}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("Danh mục", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.height(12.dp))
            FlowRow(
                Modifier.fillMaxWidth(),
                maxItemsInEachRow = 4,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Categories.of(type).forEach { cat ->
                    val selected = cat.id == categoryId
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) cat.color.copy(alpha = 0.12f) else AppTheme.finance.card)
                            .border(
                                width = 1.5.dp,
                                color = if (selected) cat.color else Color.Transparent,
                                shape = RoundedCornerShape(18.dp),
                            )
                            .clickable { categoryId = cat.id }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CategoryIcon(cat, size = 40.dp, selected = selected)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            cat.name,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                // Lấp chỗ trống để hàng cuối không bị giãn
                repeat((4 - Categories.of(type).size % 4) % 4) { Spacer(Modifier.weight(1f)) }
            }

            Spacer(Modifier.height(24.dp))
            // Ngày
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.finance.card)
                    .clickable { showDatePicker = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ngày", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Fmt.fullDate(date), style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    if (date == LocalDate.now()) "Hôm nay" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(12.dp))
            // Ghi chú
            TextField(
                value = note,
                onValueChange = { note = it.take(200) },
                placeholder = { Text("Ghi chú (không bắt buộc)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Notes, null) },
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppTheme.finance.card,
                    unfocusedContainerColor = AppTheme.finance.card,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }

        Button(
            onClick = ::submit,
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(56.dp),
        ) {
            Text(
                if (existing == null) "Lưu giao dịch" else "Cập nhật",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } },
        ) {
            DatePicker(state = state)
        }
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Rounded.DeleteOutline, null, tint = fin.expense) },
            title = { Text("Xóa giao dịch?") },
            text = { Text("Giao dịch ${Fmt.money(existing.amount)} sẽ bị xóa.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete(existing)
                }) { Text("Xóa", color = fin.expense) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Hủy") } },
        )
    }
}
