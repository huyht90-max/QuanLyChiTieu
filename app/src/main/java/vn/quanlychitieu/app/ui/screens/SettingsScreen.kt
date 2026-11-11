package vn.quanlychitieu.app.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import vn.quanlychitieu.app.BuildConfig
import vn.quanlychitieu.app.data.AppSettings
import vn.quanlychitieu.app.data.ThemeMode
import vn.quanlychitieu.app.ui.Fmt
import vn.quanlychitieu.app.ui.components.AppCard
import vn.quanlychitieu.app.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    settings: AppSettings,
    transactionCount: Int,
    contentPadding: PaddingValues,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onEditBudget: () -> Unit,
    onExport: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    var dialog by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Cài đặt",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 18.dp, start = 4.dp),
        )

        GroupLabel("Cá nhân")
        AppCard(padding = PaddingValues(vertical = 6.dp)) {
            SettingRow(
                Icons.Rounded.Person, Color(0xFF6366F1), "Tên hiển thị",
                settings.userName.ifBlank { "Chưa đặt" },
            ) { dialog = "name" }
            SettingRow(
                Icons.Rounded.Savings, Color(0xFF10B981), "Ngân sách tháng",
                if (settings.monthlyBudget > 0) Fmt.money(settings.monthlyBudget) else "Chưa đặt",
                onClick = onEditBudget,
            )
        }

        GroupLabel("Giao diện")
        AppCard(padding = PaddingValues(vertical = 6.dp)) {
            SettingRow(Icons.Rounded.DarkMode, Color(0xFF8B5CF6), "Chủ đề", settings.themeMode.label) {
                dialog = "theme"
            }
            SettingRow(
                Icons.Rounded.VisibilityOff, Color(0xFF0EA5E9), "Ẩn số tiền",
                "Che số dư khi mở ứng dụng nơi công cộng",
                trailing = {
                    Switch(
                        checked = settings.hideAmounts,
                        onCheckedChange = { v -> onUpdate { it.copy(hideAmounts = v) } },
                    )
                },
            ) { onUpdate { it.copy(hideAmounts = !it.hideAmounts) } }
        }

        GroupLabel("Dữ liệu")
        AppCard(padding = PaddingValues(vertical = 6.dp)) {
            SettingRow(
                Icons.Rounded.FileDownload, Color(0xFFF59E0B), "Xuất file CSV",
                "$transactionCount giao dịch · mở được bằng Excel",
                onClick = onExport,
            )
            SettingRow(
                Icons.Rounded.DeleteForever, AppTheme.finance.expense, "Xóa toàn bộ dữ liệu",
                "Không thể hoàn tác",
            ) { dialog = "delete" }
        }

        GroupLabel("Thông tin")
        AppCard(padding = PaddingValues(vertical = 6.dp)) {
            SettingRow(
                Icons.Rounded.Info, Color(0xFF64748B), "Phiên bản",
                "${BuildConfig.VERSION_NAME} · Dữ liệu lưu cục bộ trên máy",
                trailing = {},
            ) {}
        }
    }

    when (dialog) {
        "name" -> TextInputDialog(
            title = "Tên hiển thị",
            initial = settings.userName,
            keyboardType = KeyboardType.Text,
            onDismiss = { dialog = null },
            onConfirm = { v -> onUpdate { it.copy(userName = v.trim().take(30)) }; dialog = null },
        )

        "theme" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Chủ đề") },
            text = {
                Column {
                    ThemeMode.entries.forEach { m ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .selectable(
                                    selected = settings.themeMode == m,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onUpdate { it.copy(themeMode = m) }
                                        dialog = null
                                    },
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = settings.themeMode == m, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(m.label)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Đóng") } },
        )

        "delete" -> AlertDialog(
            onDismissRequest = { dialog = null },
            icon = { Icon(Icons.Rounded.DeleteForever, null, tint = AppTheme.finance.expense) },
            title = { Text("Xóa toàn bộ dữ liệu?") },
            text = { Text("Tất cả $transactionCount giao dịch sẽ bị xóa vĩnh viễn. Bạn nên xuất CSV để sao lưu trước.") },
            confirmButton = {
                TextButton(onClick = { onDeleteAll(); dialog = null }) {
                    Text("Xóa hết", color = AppTheme.finance.expense)
                }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Hủy") } },
        )
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (trailing != null) trailing()
        else Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun TextInputDialog(
    title: String,
    initial: String,
    keyboardType: KeyboardType,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    capitalization = KeyboardCapitalization.Words,
                ),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value) }) { Text("Lưu") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
    )
}

/** Hộp thoại nhập ngân sách tháng, có định dạng phân cách hàng nghìn. */
@Composable
fun BudgetDialog(current: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var digits by remember { mutableStateOf(if (current > 0) current.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Savings, null) },
        title = { Text("Ngân sách tháng") },
        text = {
            Column {
                Text(
                    "Đặt hạn mức chi tiêu mỗi tháng. Ứng dụng sẽ cảnh báo khi bạn chi gần hết.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = if (digits.isEmpty()) "" else Fmt.number(digits.toLong()),
                    onValueChange = { digits = it.filter(Char::isDigit).trimStart('0').take(13) },
                    singleLine = true,
                    suffix = { Text("₫") },
                    placeholder = { Text("VD: 10.000.000") },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5_000_000L, 10_000_000L, 20_000_000L).forEach { v ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable { digits = v.toString() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) { Text(Fmt.compact(v), style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(digits.toLongOrNull() ?: 0) }) { Text("Lưu") } },
        dismissButton = {
            if (current > 0) TextButton(onClick = { onConfirm(0) }) { Text("Bỏ ngân sách") }
            else TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}
