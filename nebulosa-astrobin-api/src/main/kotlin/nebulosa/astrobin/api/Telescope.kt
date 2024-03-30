package nebulosa.astrobin.api

data class Telescope(
    override val id: Long = 0L,
    override val brandName: String = "",
    override val name: String = "",
    val type: String = "",
    val aperture: Double = 0.0,
    val minFocalLength: Double = 0.0,
    val maxFocalLength: Double = 0.0,
) : Equipment
