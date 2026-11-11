package vn.quanlychitieu.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

private class DbHelper(context: Context) :
    SQLiteOpenHelper(context, "chitieu.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                category TEXT NOT NULL,
                note TEXT NOT NULL DEFAULT '',
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_tx_time ON transactions(timestamp)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

/** Nguồn dữ liệu duy nhất cho giao dịch. Danh sách luôn sắp xếp mới nhất trước. */
class TransactionRepository(context: Context) {
    private val helper = DbHelper(context.applicationContext)
    private val _items = MutableStateFlow<List<Transaction>>(emptyList())
    val items: StateFlow<List<Transaction>> = _items.asStateFlow()

    suspend fun load() = withContext(Dispatchers.IO) {
        _items.value = queryAll()
    }

    suspend fun upsert(tx: Transaction): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("type", tx.type.name)
            put("amount", tx.amount)
            put("category", tx.categoryId)
            put("note", tx.note)
            put("timestamp", tx.timestamp)
        }
        val db = helper.writableDatabase
        val id = if (tx.id == 0L) {
            db.insert("transactions", null, values)
        } else {
            db.update("transactions", values, "id = ?", arrayOf(tx.id.toString()))
            tx.id
        }
        _items.value = queryAll()
        id
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete("transactions", "id = ?", arrayOf(id.toString()))
        _items.value = queryAll()
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete("transactions", null, null)
        _items.value = emptyList()
    }

    private fun queryAll(): List<Transaction> {
        val result = ArrayList<Transaction>()
        helper.readableDatabase.query(
            "transactions", null, null, null, null, null, "timestamp DESC, id DESC"
        ).use { c ->
            val iId = c.getColumnIndexOrThrow("id")
            val iType = c.getColumnIndexOrThrow("type")
            val iAmount = c.getColumnIndexOrThrow("amount")
            val iCat = c.getColumnIndexOrThrow("category")
            val iNote = c.getColumnIndexOrThrow("note")
            val iTime = c.getColumnIndexOrThrow("timestamp")
            while (c.moveToNext()) {
                result += Transaction(
                    id = c.getLong(iId),
                    type = runCatching { TxType.valueOf(c.getString(iType)) }
                        .getOrDefault(TxType.EXPENSE),
                    amount = c.getLong(iAmount),
                    categoryId = c.getString(iCat),
                    note = c.getString(iNote),
                    timestamp = c.getLong(iTime),
                )
            }
        }
        return result
    }
}
