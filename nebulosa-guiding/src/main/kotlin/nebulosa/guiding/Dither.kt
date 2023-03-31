package nebulosa.guiding

interface Dither {

    fun get(amount: Double, raOnly: Boolean = false): DoubleArray

    fun reset()
}
