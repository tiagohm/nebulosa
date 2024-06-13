package nebulosa.watney.platesolver.quad

import nebulosa.math.Angle

data class CoordinateBounds(
    @JvmField val left: Angle = 0.0, @JvmField val right: Angle = 0.0, // RA
    @JvmField val top: Angle = 0.0, @JvmField val bottom: Angle = 0.0, // DEC
) {

    @JvmField val rightAscension: Angle = left + (right - left) / 2

    @JvmField val declination: Angle = bottom + (top - bottom) / 2

    fun contains(rightAscension: Angle, declination: Angle) =
        rightAscension in left..right && declination in bottom..top

    operator fun contains(bounds: CoordinateBounds) =
        contains(bounds.rightAscension, bounds.declination)
}
