package nebulosa.math

import java.util.*
import kotlin.math.abs

data class AngleFormatter(
    private val isHours: Boolean = false,
    private val hasSign: Boolean = true,
    private val hasSeconds: Boolean = true,
    private val secondsDecimalPlaces: Int = 1,
    private val separators: List<String> = emptyList(),
    private val locale: Locale = Locale.ROOT,
) {

    constructor(builder: Builder) : this(
        builder.isHours,
        builder.hasSign,
        builder.hasSeconds,
        builder.secondsDecimalPlaces,
        builder.separators.toList(),
        builder.locale,
    )

    fun format(angle: Angle): String {
        val (a, b, c) = if (isHours) angle.hms() else angle.dms()
        val sign = if (hasSign) if (a < 0) "-" else "+" else ""
        val s0 = if (separators.isNotEmpty()) separators[0] else if (isHours) ":" else ""
        val s1 = if (separators.size > 1) separators[1] else if (!hasSeconds && s0.trim() == ":") "" else s0
        val k = if (secondsDecimalPlaces == 0) 1 else 0
        val seconds = if (hasSeconds) "%0${secondsDecimalPlaces + 3 - k}.${secondsDecimalPlaces}f".format(locale, c) else ""
        val s2 = if (separators.size > 2) separators[2] else ""
        return "%s%d%s%02d%s%s%s".format(locale, sign, abs(a).toInt(), s0, b.toInt(), s1, seconds, s2).trim()
    }

    class Builder {

        internal var isHours = false
        internal var hasSign = true
        internal var hasSeconds = true
        internal var secondsDecimalPlaces = 1
        internal val separators = arrayListOf<String>()
        internal var locale = Locale.ROOT

        fun degrees() = apply { isHours = false }

        fun hours() = apply { isHours = true }

        fun noSign() = apply { hasSign = false }

        fun noSeconds() = apply { hasSeconds = false }

        fun secondsDecimalPlaces(n: Int) = apply { secondsDecimalPlaces = n }

        fun separators(vararg chars: String) = apply { separators.addAll(chars) }

        fun locale(locale: Locale) = apply { this.locale = locale }

        fun build() = AngleFormatter(this)
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

        @JvmStatic val DMS = Builder()
            .degrees()
            .noSign()
            .secondsDecimalPlaces(1)
            .separators("°", "'", "\"")
            .build()
    }
}
