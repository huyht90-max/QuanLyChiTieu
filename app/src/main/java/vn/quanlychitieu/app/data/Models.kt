package vn.quanlychitieu.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TxType { EXPENSE, INCOME }

data class Transaction(
    val id: Long = 0,
    val type: TxType,
    val amount: Long,
    val categoryId: String,
    val note: String,
    val timestamp: Long,
)

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: TxType,
)

object Categories {
    val expense = listOf(
        Category("food", "Ăn uống", Icons.Rounded.Restaurant, Color(0xFFF97316), TxType.EXPENSE),
        Category("coffee", "Cà phê", Icons.Rounded.LocalCafe, Color(0xFFA16207), TxType.EXPENSE),
        Category("transport", "Di chuyển", Icons.Rounded.DirectionsCar, Color(0xFF0EA5E9), TxType.EXPENSE),
        Category("shopping", "Mua sắm", Icons.Rounded.ShoppingBag, Color(0xFFEC4899), TxType.EXPENSE),
        Category("bills", "Hóa đơn", Icons.AutoMirrored.Rounded.ReceiptLong, Color(0xFF8B5CF6), TxType.EXPENSE),
        Category("home", "Nhà cửa", Icons.Rounded.Home, Color(0xFF14B8A6), TxType.EXPENSE),
        Category("health", "Sức khỏe", Icons.Rounded.LocalHospital, Color(0xFFEF4444), TxType.EXPENSE),
        Category("education", "Học tập", Icons.Rounded.School, Color(0xFF3B82F6), TxType.EXPENSE),
        Category("entertainment", "Giải trí", Icons.Rounded.SportsEsports, Color(0xFFA855F7), TxType.EXPENSE),
        Category("travel", "Du lịch", Icons.Rounded.Flight, Color(0xFF06B6D4), TxType.EXPENSE),
        Category("family", "Gia đình", Icons.Rounded.FamilyRestroom, Color(0xFFF59E0B), TxType.EXPENSE),
        Category("gift", "Quà tặng", Icons.Rounded.CardGiftcard, Color(0xFFE11D48), TxType.EXPENSE),
        Category("beauty", "Làm đẹp", Icons.Rounded.Spa, Color(0xFFD946EF), TxType.EXPENSE),
        Category("pet", "Thú cưng", Icons.Rounded.Pets, Color(0xFF84CC16), TxType.EXPENSE),
        Category("other_exp", "Khác", Icons.Rounded.MoreHoriz, Color(0xFF64748B), TxType.EXPENSE),
    )

    val income = listOf(
        Category("salary", "Lương", Icons.Rounded.Payments, Color(0xFF10B981), TxType.INCOME),
        Category("bonus", "Thưởng", Icons.Rounded.EmojiEvents, Color(0xFFF59E0B), TxType.INCOME),
        Category("business", "Kinh doanh", Icons.Rounded.Storefront, Color(0xFF6366F1), TxType.INCOME),
        Category("invest", "Đầu tư", Icons.AutoMirrored.Rounded.TrendingUp, Color(0xFF0EA5E9), TxType.INCOME),
        Category("gift_in", "Được tặng", Icons.Rounded.Redeem, Color(0xFFEC4899), TxType.INCOME),
        Category("other_in", "Khác", Icons.Rounded.MoreHoriz, Color(0xFF64748B), TxType.INCOME),
    )

    private val all = (expense + income).associateBy { it.id }

    fun of(type: TxType) = if (type == TxType.EXPENSE) expense else income

    fun get(id: String): Category = all[id] ?: expense.last()
}
