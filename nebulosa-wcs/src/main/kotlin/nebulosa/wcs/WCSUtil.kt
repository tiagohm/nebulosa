package nebulosa.wcs

import nebulosa.fits.FitsKeywordDictionary
import nebulosa.image.format.ReadableHeader
import nebulosa.math.Angle
import nebulosa.math.cos
import nebulosa.math.deg
import nebulosa.math.sin
import kotlin.math.abs
import kotlin.math.sign

val ReadableHeader.hasCd
    get() = "CD1_1" in this ||
            "CDELT1" in this && "CROTA2" in this ||
            "CDELT1" in this && "PC1_1" in this

fun ReadableHeader.computeCdMatrix(): DoubleArray {
    return if (hasCd) {
        doubleArrayOf(cd(1, 1), cd(1, 2), cd(2, 1), cd(2, 2))
    } else {
        val a = getDouble(FitsKeywordDictionary.CDELT1, 0.0)
        val b = getDouble(FitsKeywordDictionary.CDELT2, 0.0)
        val c = getDouble(FitsKeywordDictionary.CROTA2, 0.0).deg
        computeCdFromCdelt(a, b, c)
    }
}

fun ReadableHeader.cd(i: Int, j: Int): Double {
    return if ("CD1_1" in this) {
        getDouble("CD${i}_$j", 0.0)
    } else if ("CROTA2" in this) {
        val a = getDouble("CDELT1", 0.0)
        val b = getDouble("CDELT2", 0.0)
        val c = getDouble("CROTA2", 0.0).deg
        val cd = computeCdFromCdelt(a, b, c)
        cd[2 * i + j - 3]
    } else if ("PC1_1" in this) {
        val pc11 = getDouble("PC1_1", 0.0)
        val pc12 = getDouble("PC1_2", 0.0)
        val pc21 = getDouble("PC2_1", 0.0)
        val pc22 = getDouble("PC2_2", 0.0)
        val a = getDouble("CDELT1", 0.0)
        val b = getDouble("CDELT2", 0.0)
        val cd = pc2cd(pc11, pc12, pc21, pc22, a, b)
        cd[2 * i + j - 3]
    } else {
        throw IllegalArgumentException("cd[$i,$j] not found")
    }
}

private fun computeCdFromCdelt(cdelt1: Double, cdelt2: Double, crota: Angle): DoubleArray {
    val cos0 = crota.cos
    val sin0 = crota.sin
    val cd11 = cdelt1 * cos0
    val cd12 = abs(cdelt2) * sign(cdelt1) * sin0
    val cd21 = -abs(cdelt1) * sign(cdelt2) * sin0
    val cd22 = cdelt2 * cos0
    return doubleArrayOf(cd11, cd12, cd21, cd22)
}

private fun pc2cd(
    pc11: Double, pc10: Double,
    pc21: Double, pc22: Double,
    cdelt1: Double, cdelt2: Double,
) = doubleArrayOf(cdelt1 * pc11, cdelt2 * pc21, cdelt1 * pc10, cdelt2 * pc22)

