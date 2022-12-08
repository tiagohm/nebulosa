package nebulosa.api.atlas

sealed interface Body {

    val x: Double
    val y: Double
    val z: Double

    val vx: Double
    val vy: Double
    val vz: Double

    val raJ2000: Double
    val decJ2000: Double

    val ra: Double
    val dec: Double

    data class Sun(
        override val x: Double, override val y: Double, override val z: Double,
        override val vx: Double, override val vy: Double, override val vz: Double,
        override val raJ2000: Double, override val decJ2000: Double,
        override val ra: Double, override val dec: Double,
    ) : Body

    data class Moon(
        override val x: Double, override val y: Double, override val z: Double,
        override val vx: Double, override val vy: Double, override val vz: Double,
        override val raJ2000: Double, override val decJ2000: Double,
        override val ra: Double, override val dec: Double,
    ) : Body
}
