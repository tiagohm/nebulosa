package nebulosa.time

import nebulosa.io.bufferedResource
import nebulosa.io.readDoubleArrayLe

/**
 * Polynomial Coeffieints for ∆T and length of day.
 *
 * @see <a href="http://astro.ukho.gov.uk/nao/lvm/Poly-Y6.pdf">PDF Documentation</a>
 * @see <a href="http://astro.ukho.gov.uk/nao/lvm/Table-S15.2020.txt">Download</a>
 */
data object S15 : Spline<DoubleArray> by MultiSpline(bufferedResource("S15.dat") { (0..5).map { readDoubleArrayLe(58) } })
