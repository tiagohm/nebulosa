package nebulosa.fits.algorithms

import kotlin.math.exp

fun gaussian2D(sigmaSquared: Double, x: Int, y: Int): Double {
    return exp((x * x + y * y) / (-2.0 * sigmaSquared)) / (2.0 * Math.PI * sigmaSquared)
}
