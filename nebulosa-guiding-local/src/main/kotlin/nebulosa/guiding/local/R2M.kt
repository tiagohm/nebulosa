package nebulosa.guiding.local

data class R2M(
    @JvmField val px: Float,
    @JvmField val py: Float,
    @JvmField val m: Float,
) : Comparable<R2M> {

    @JvmField internal var r2 = 0f

    override fun compareTo(other: R2M) = r2.compareTo(other.r2)
}
