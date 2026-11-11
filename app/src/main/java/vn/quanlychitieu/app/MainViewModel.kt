package vn.quanlychitieu.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.quanlychitieu.app.data.AppSettings
import vn.quanlychitieu.app.data.Categories
import vn.quanlychitieu.app.data.SettingsStore
import vn.quanlychitieu.app.data.Transaction
import vn.quanlychitieu.app.data.TransactionRepository
import vn.quanlychitieu.app.data.TxType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = TransactionRepository(app)
    private val settingsStore = SettingsStore(app)

    val transactions: StateFlow<List<Transaction>> = repo.items
    val settings: StateFlow<AppSettings> = settingsStore.state

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    init {
        viewModelScope.launch {
            repo.load()
            _loaded.value = true
        }
    }

    fun save(tx: Transaction) {
        viewModelScope.launch { repo.upsert(tx) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun deleteAll() {
        viewModelScope.launch { repo.deleteAll() }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) = settingsStore.update(transform)

    /** Xuất toàn bộ giao dịch ra CSV (UTF-8 có BOM để Excel hiển thị đúng tiếng Việt). */
    fun exportCsv(uri: Uri, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val sb = StringBuilder("﻿Ngày,Loại,Danh mục,Số tiền,Ghi chú\n")
                    transactions.value.forEach { t ->
                        val date = Instant.ofEpochMilli(t.timestamp)
                            .atZone(ZoneId.systemDefault()).format(fmt)
                        val type = if (t.type == TxType.EXPENSE) "Chi" else "Thu"
                        val note = "\"" + t.note.replace("\"", "\"\"") + "\""
                        sb.append(date).append(',').append(type).append(',')
                            .append(Categories.get(t.categoryId).name).append(',')
                            .append(t.amount).append(',').append(note).append('\n')
                    }
                    val out = getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?: error("Không mở được file")
                    out.use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
                }.isSuccess
            }
            onDone(ok)
        }
    }
}
