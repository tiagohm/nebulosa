package nebulosa.math

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.abs

data class AngleFormatter(
    private val isHours: Boolean = false,
    private val hasSign: Boolean = true,
    private val hoursFormat: String = "%02d",
    private val degreesFormat: String = "%03d",
    private val minutesFormat: String = "%02d",
    private val secondsFormat: String = "",
    private val hasSeconds: Boolean = true,
    private val secondsDecimalPlaces: Int = 1,
    private val separators: List<String> = emptyList(),
    private val locale: Locale = Locale.ROOT,
    private val minusSign: String = "-",
    private val plusSign: String = "+",
) {

    constructor(builder: Builder) : this(
        builder.isHours,
        builder.hasSign,
        builder.hoursFormat,
        builder.degreesFormat,
        builder.minutesFormat,
        builder.secondsFormat,
        builder.hasSeconds,
        builder.secondsDecimalPlaces,
        builder.separators.toList(),
        builder.locale,
        builder.minusSign,
        builder.plusSign,
    )

    fun format(angle: Angle): String {
        val (a, b, c) = if (isHours) angle.hms() else angle.dms()
        val sign = if (hasSign) if (a < 0) minusSign else plusSign else ""
        val s0 = if (separators.isNotEmpty()) separators[0] else if (isHours) ":" else ""
        val s1 = if (separators.size > 1) separators[1] else if (!hasSeconds && s0.trim() == ":") "" else s0
        val k = if (secondsDecimalPlaces == 0) 1 else 0
        val d = if (c >= 59.0) BigDecimal.valueOf(c).setScale(secondsDecimalPlaces, RoundingMode.DOWN).toDouble() else c
        val seconds = if (hasSeconds) secondsFormat.ifBlank { "%0${secondsDecimalPlaces + 3 - k}.${secondsDecimalPlaces}f" }.format(locale, d)
        else ""
        val s2 = if (separators.size > 2) separators[2] else ""
        val format0 = if (isHours) hoursFormat else degreesFormat

        return "%s$format0%s$minutesFormat%s%s%s".format(locale, sign, abs(a).toInt(), s0, b.toInt(), s1, seconds, s2)
            .trim()
    }

    fun newBuilder() = Builder(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AngleFormatter) return false

        if (isHours != other.isHours) return false
        if (hasSign != other.hasSign) return false
        if (hoursFormat != other.hoursFormat) return false
        if (degreesFormat != other.degreesFormat) return false
        if (minutesFormat != other.minutesFormat) return false
        if (secondsFormat != other.secondsFormat) return false
        if (hasSeconds != other.hasSeconds) return false
        if (secondsDecimalPlaces != other.secondsDecimalPlaces) return false
        if (separators != other.separators) return false
        if (locale != other.locale) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isHours.hashCode()
        result = 31 * result + hasSign.hashCode()
        result = 31 * result + hoursFormat.hashCode()
        result = 31 * result + degreesFormat.hashCode()
        result = 31 * result + minutesFormat.hashCode()
        result = 31 * result + secondsFormat.hashCode()
        result = 31 * result + hasSeconds.hashCode()
        result = 31 * result + secondsDecimalPlaces
        result = 31 * result + separators.hashCode()
        result = 31 * result + locale.hashCode()
        return result
    }

    class Builder internal constructor(formatter: AngleFormatter?) {

        internal var isHours = formatter?.isHours == true
        internal var hoursFormat = formatter?.hoursFormat ?: "%02d"
        internal var degreesFormat = formatter?.degreesFormat ?: "%03d"
        internal var minutesFormat = formatter?.minutesFormat ?: "%02d"
        internal var secondsFormat = formatter?.secondsFormat ?: ""
        internal var hasSign = formatter?.hasSign != false
        internal var hasSeconds = formatter?.hasSeconds != false
        internal var secondsDecimalPlaces = formatter?.secondsDecimalPlaces ?: 1
        internal val separators = formatter?.separators?.toMutableList() ?: arrayListOf()
        internal var locale = formatter?.locale ?: Locale.ROOT
        internal var minusSign = "-"
        internal var plusSign = "+"

        constructor() : this(null)

        fun degrees() = apply { isHours = false }

        fun hours() = apply { isHours = true }

        fun hoursFormat(format: String) = apply { require(format.isNotBlank()); hoursFormat = format }

        fun degreesFormat(format: String) = apply { require(format.isNotBlank()); degreesFormat = format }

        fun minutesFormat(format: String) = apply { require(format.isNotBlank()); minutesFormat = format }

        fun secondsFormat(format: String) = apply { require(format.isNotBlank()); secondsFormat = format }

        fun noSign() = apply { hasSign = false }

        fun noSeconds() = apply { hasSeconds = false }

        fun secondsDecimalPlaces(n: Int) = apply { require(n >= 0); secondsFormat = ""; secondsDecimalPlaces = n }

        fun separators(vararg chars: String) = apply { separators.clear(); separators.addAll(chars) }

        fun whitespaced() = separators(" ")

        fun locale(locale: Locale) = apply { this.locale = locale }

        fun minusSign(sign: String) = apply { minusSign = sign }

        fun plusSign(sign: String) = apply { plusSign = sign }

        fun build() = AngleFormatter(this)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Builder

            if (isHours != other.isHours) return false
            if (hoursFormat != other.hoursFormat) return false
            if (degreesFormat != other.degreesFormat) return false
            if (minutesFormat != other.minutesFormat) return false
            if (secondsFormat != other.secondsFormat) return false
            if (hasSign != other.hasSign) return false
            if (hasSeconds != other.hasSeconds) return false
            if (secondsDecimalPlaces != other.secondsDecimalPlaces) return false
            if (separators != other.separators) return false
            if (locale != other.locale) return false
            if (minusSign != other.minusSign) return false
            if (plusSign != other.plusSign) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isHours.hashCode()
            result = 31 * result + hoursFormat.hashCode()
            result = 31 * result + degreesFormat.hashCode()
            result = 31 * result + minutesFormat.hashCode()
            result = 31 * result + secondsFormat.hashCode()
            result = 31 * result + hasSign.hashCode()
            result = 31 * result + hasSeconds.hashCode()
            result = 31 * result + secondsDecimalPlaces
            result = 31 * result + separators.hashCode()
            result = 31 * result + (locale?.hashCode() ?: 0)
            result = 31 * result + minusSign.hashCode()
            result = 31 * result + plusSign.hashCode()
            return result
        }
    }

    companion object {

        @JvmStatic val HMS = Builder()
            .hours()
            .noSign()
            .secondsDecimalPlaces(1)
            .separators("h", "m", "s")
            .build()

        @JvmStatic val SIGNED_DMS = Builder()
            .degrees()
            .secondsDecimalPlaces(1)
            .separators("°", "'", "\"")
            .build()

        @JvmStatic val DMS = SIGNED_DMS
            .newBuilder()
            .noSign()
            .build()
    }
}
