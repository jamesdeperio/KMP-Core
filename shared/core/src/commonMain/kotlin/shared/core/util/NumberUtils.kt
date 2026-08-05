package shared.core.util

import kotlin.math.roundToLong

/**
 * Formats a Double as a currency string (e.g., 12.5 -> "$12.50").
 * Note: For production with complex localization, consider an expect/actual DecimalFormat.
 */
fun Double.formatPrice(currency: String = "$", decimals: Int = 2): String {
    val factor = 10.0.pow(decimals)
    val rounded = (this * factor).roundToLong() / factor
    val string = rounded.toString()
    val parts = string.split(".")
    
    val integerPart = parts[0]
    val fractionalPart = parts.getOrNull(1) ?: ""
    
    return "$currency$integerPart.${fractionalPart.padEnd(decimals, '0')}"
}

/**
 * Basic power function since it's not always in commonMain math for all targets.
 */
private fun Double.pow(exponent: Int): Double {
    var res = 1.0
    repeat(exponent) { res *= this }
    return res
}

/**
 * Returns a percentage string (e.g., 0.125 -> "12.5%").
 */
fun Double.toPercentage(decimals: Int = 1): String {
    val percent = this * 100
    val factor = 10.0.pow(decimals)
    val rounded = (percent * factor).roundToLong() / factor
    return "$rounded%"
}
