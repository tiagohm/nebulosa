package nebulosa.indi.protocol

sealed interface HasMinMax<T : Comparable<T>> : ClosedRange<T> {

    val min: T

    val max: T

    override val start
        get() = min

    override val endInclusive
        get() = max
}
