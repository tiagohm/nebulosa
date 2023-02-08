package nebulosa.nasa.daf

class Summary(
    val name: String,
    private val doubles: DoubleArray,
    private val ints: IntArray,
) {

    val numberOfDoubles
        get() = doubles.size

    val numberOfInts
        get() = ints.size

    fun doubleAt(index: Int) = doubles[index]

    fun intAt(index: Int) = ints[index]
}
