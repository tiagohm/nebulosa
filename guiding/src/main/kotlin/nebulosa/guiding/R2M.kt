package nebulosa.guiding

data class R2M(
    @JvmField val px: Int,
    @JvmField val py: Int,
    @JvmField val m: Float,
) : Comparable<R2M> {

    @JvmField internal var r2 = 0

    override fun compareTo(other: R2M) = r2.compareTo(other.r2)
}
