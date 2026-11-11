package vn.quanlychitieu.app.ui

import vn.quanlychitieu.app.data.Transaction
import vn.quanlychitieu.app.data.TxType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

object Fmt {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val grouped = DecimalFormat("#,###", symbols)
    private val oneDecimal = DecimalFormat("#,##0.#", symbols)

    /** 1250000 -> "1.250.000" */
    fun number(value: Long): String = grouped.format(value)

    /** 1250000 -> "1.250.000 ₫" */
    fun money(value: Long, hidden: Boolean = false): String =
        if (hidden) "••••••" else "${number(value)} ₫"

    /** Có dấu +/-: "+1.250.000 ₫" */
    fun signed(value: Long, type: TxType, hidden: Boolean = false): String =
        if (hidden) "••••••" else (if (type == TxType.INCOME) "+" else "−") + money(abs(value))

    /** Rút gọn cho biểu đồ: 1.2tr, 850K, 2,5 tỷ */
    fun compact(value: Long): String {
        val v = abs(value).toDouble()
        val s = when {
            v >= 1_000_000_000 -> oneDecimal.format(v / 1_000_000_000) + " tỷ"
            v >= 1_000_000 -> oneDecimal.format(v / 1_000_000) + "tr"
            v >= 1_000 -> oneDecimal.format(v / 1_000) + "K"
            else -> grouped.format(v)
        }
        return if (value < 0) "-$s" else s
    }

    private val zone get() = ZoneId.systemDefault()

    fun date(ts: Long): LocalDate = Instant.ofEpochMilli(ts).atZone(zone).toLocalDate()

    fun yearMonth(ts: Long): YearMonth = YearMonth.from(date(ts))

    fun monthTitle(ym: YearMonth): String {
        val now = YearMonth.now()
        return when (ym) {
            now -> "Tháng này"
            now.minusMonths(1) -> "Tháng trước"
            else -> "Tháng ${ym.monthValue}/${ym.year}"
        }
    }

    private val dayFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun weekday(d: LocalDate): String = when (d.dayOfWeek.value) {
        1 -> "Thứ Hai"; 2 -> "Thứ Ba"; 3 -> "Thứ Tư"; 4 -> "Thứ Năm"
        5 -> "Thứ Sáu"; 6 -> "Thứ Bảy"; else -> "Chủ Nhật"
    }

    fun dayHeader(d: LocalDate): String {
        val today = LocalDate.now()
        return when (d) {
            today -> "Hôm nay"
            today.minusDays(1) -> "Hôm qua"
            else -> "${weekday(d)}, ${d.format(dayFmt)}"
        }
    }

    fun shortDate(d: LocalDate): String {
        val today = LocalDate.now()
        return when (d) {
            today -> "Hôm nay"
            today.minusDays(1) -> "Hôm qua"
            else -> d.format(dayFmt)
        }
    }

    fun time(ts: Long): String = Instant.ofEpochMilli(ts).atZone(zone).format(timeFmt)

    fun fullDate(d: LocalDate): String = "${weekday(d)}, ${d.format(dayFmt)}"
}

data class Summary(val income: Long, val expense: Long) {
    val balance get() = income - expense
}

fun List<Transaction>.summary(): Summary {
    var inc = 0L
    var exp = 0L
    forEach { if (it.type == TxType.INCOME) inc += it.amount else exp += it.amount }
    return Summary(inc, exp)
}

fun List<Transaction>.inMonth(ym: YearMonth) = filter { Fmt.yearMonth(it.timestamp) == ym }
