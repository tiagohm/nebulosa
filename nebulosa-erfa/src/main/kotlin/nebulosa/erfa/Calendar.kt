package nebulosa.erfa

data class Calendar(
    @JvmField val year: Int,
    @JvmField val month: Int,
    @JvmField val day: Int,
    @JvmField val fraction: Double,
)
