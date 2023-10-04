package nebulosa.guiding.internal

import kotlin.random.Random

class RandomDither(private val random: Random = Random.Default) : Dither {

    override fun get(amount: Double, raOnly: Boolean): DoubleArray {
        val ra = amount * ((random.nextInt() / MAX_RANDOM) * 2.0 - 1.0)
        val dec = if (raOnly) 0.0 else amount * ((random.nextInt() / MAX_RANDOM) * 2.0 - 1.0)
        return doubleArrayOf(ra, dec)
    }

    override fun reset() {}

    companion object {

        @JvmStatic private val MAX_RANDOM = Int.MAX_VALUE.toDouble()
    }
}
