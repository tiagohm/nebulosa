package nebulosa.time

@JvmInline
value class JulianCalendarCutOff(val value: Int) {

    companion object {

        val NONE = JulianCalendarCutOff(Int.MIN_VALUE)

        val GREGORIAN_START = JulianCalendarCutOff(2299161)

        val GREGORIAN_START_ENGLAND = JulianCalendarCutOff(2361222)
    }
}
