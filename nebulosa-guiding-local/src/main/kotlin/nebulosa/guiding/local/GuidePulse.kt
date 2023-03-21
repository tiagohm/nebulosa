package nebulosa.guiding.local

interface GuidePulse {

    fun pulseNorth(duration: Long)

    fun pulseSouth(duration: Long)

    fun pulseWest(duration: Long)

    fun pulseEast(duration: Long)
}
