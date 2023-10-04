package nebulosa.guiding.internal

interface Dither {

    fun get(amount: Double, raOnly: Boolean = false): DoubleArray

    fun reset()
}
