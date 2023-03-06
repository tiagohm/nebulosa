package nebulosa.wcs

import nebulosa.constants.RAD2DEG
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.PairOfAngle
import nebulosa.wcs.projection.AbstractProjection
import nebulosa.wcs.projection.Projection
import nebulosa.wcs.projection.ProjectionType
import nom.tam.fits.Header
import kotlin.math.abs
import kotlin.math.sign

// TODO: Remove FITS dependency?
class WCSTransform(@JvmField internal val header: Header) {

    private val projection: Projection
    private val cd: Array<DoubleArray>
    private val cdi: Array<DoubleArray>

    val lonpole = if (header.containsKey("LONPOLE")) header.getDoubleValue("LONPOLE")
    else if (header.containsKey("PV1_3")) header.getDoubleValue("PV1_3")
    else Double.NaN

    val latpole = if (header.containsKey("LATPOLE")) header.getDoubleValue("LATPOLE")
    else if (header.containsKey("PV1_4")) header.getDoubleValue("PV1_4")
    else Double.NaN

    val hasCd = header.containsKey("CD1_1") ||
            header.containsKey("CDELT1") && header.containsKey("CROTA2") ||
            header.containsKey("CDELT1") && header.containsKey("PC1_1")

    val crpix1 = header.getDoubleValue("CRPIX1")

    val crpix2 = header.getDoubleValue("CRPIX2")

    init {
        val ctype1 = header.getStringValue("CTYPE1")
        val projectionType = ProjectionType.valueOf(ctype1.substring(ctype1.lastIndexOf('-') + 1, ctype1.length))
        val cx = header.getStringValue("CUNIT1").convertCunitToDegrees()
        val cy = header.getStringValue("CUNIT2").convertCunitToDegrees()
        projection = createProjection(this, projectionType, cx, cy) as AbstractProjection

        if (projectionType != ProjectionType.TPV) {
            // Native longitude of the fiducial point.
            if (header.containsKey("PV1_1")) {
                projection.phi0 = header.getDoubleValue("PV1_1").rad
            }
            // Native latitude of the celestial pole.
            if (header.containsKey("PV1_2")) {
                projection.theta0 = header.getDoubleValue("PV1_2").rad
            }
        }

        // Native longitude of the celestial pole.
        if (lonpole.isFinite()) projection.phip = lonpole.deg

        // Native latitude of the celestial pole.
        if (latpole.isFinite()) projection.thetap = latpole.deg

        cd = createCdMatrix()
        cdi = inverseCd(cd)
    }

    private fun createCdMatrix(): Array<DoubleArray> {
        return if (hasCd) {
            arrayOf(
                doubleArrayOf(cd(1, 1), cd(1, 2)),
                doubleArrayOf(cd(2, 1), cd(2, 2)),
            )
        } else {
            val a = header.getDoubleValue("CDELT1")
            val b = header.getDoubleValue("CDELT2")
            val c = header.getDoubleValue("CROTA2").deg
            computeCdFromCdelt(a, b, c)
        }
    }

    private fun cd(i: Int, j: Int): Double {
        return if (header.containsKey("CD1_1")) {
            header.getDoubleValue("CD${i}_$j")
        } else if (header.containsKey("CROTA2")) {
            val a = header.getDoubleValue("CDELT1")
            val b = header.getDoubleValue("CDELT2")
            val c = header.getDoubleValue("CROTA2").deg
            val cd = computeCdFromCdelt(a, b, c)
            cd[i - 1][j - 1]
        } else if (header.containsKey("PC1_1")) {
            val pc11 = header.getDoubleValue("PC1_1")
            val pc12 = header.getDoubleValue("PC1_2")
            val pc21 = header.getDoubleValue("PC2_1")
            val pc22 = header.getDoubleValue("PC2_2")
            val a = header.getDoubleValue("CDELT1")
            val b = header.getDoubleValue("CDELT2")
            val cd = pc2cd(pc11, pc12, pc21, pc22, a, b)
            cd[i - 1][j - 1]
        } else {
            throw IllegalArgumentException("cd[$i,$j] not found")
        }
    }

    private fun inverseCd(cd: Array<DoubleArray>): Array<DoubleArray> {
        val det = (cd[0][0] * cd[1][1] - cd[0][1] * cd[1][0])
        return arrayOf(
            doubleArrayOf(cd[1][1] / det, -cd[0][1] / det),
            doubleArrayOf(-cd[1][0] / det, cd[0][0] / det),
        )
    }

    fun pixelToWorld(x: Double, y: Double): PairOfAngle {
        val cx = x - crpix1
        val cy = y - crpix2
        val v0 = cx * cd[0][0] + cy * cd[0][1]
        val v1 = cx * cd[1][0] + cy * cd[1][1]
        return projection.computeCelestialSphericalCoordinate(v0, v1)
    }

    fun worldToPixel(rightAscension: Angle, declination: Angle): DoubleArray {
        val coord = projection.computeProjectionPlaneCoordinate(rightAscension, declination)
        val v0 = coord[0] * cdi[0][0] + coord[1] * cdi[0][1]
        val v1 = coord[0] * cdi[1][0] + coord[1] * cdi[1][1]
        return doubleArrayOf(v0 + crpix1, v1 + crpix2)
    }

    fun inside(longitude: Angle, latitude: Angle): Boolean {
        return projection.inside(longitude, latitude)
    }

    companion object {

        @JvmStatic
        private fun String?.convertCunitToDegrees() = when (this) {
            null -> 1.0
            "arcmin" -> 1.0 / 60.0
            "arcsec" -> 1.0 / 3600.0
            "mas" -> 1.0 / 3600000.0
            "rad" -> RAD2DEG
            else -> 1.0
        }

        @JvmStatic
        private fun computeCdFromCdelt(cdelt1: Double, cdelt2: Double, crota: Angle): Array<DoubleArray> {
            val cos0 = crota.cos
            val sin0 = crota.sin
            val cd11 = cdelt1 * cos0
            val cd12 = abs(cdelt2) * sign(cdelt1) * sin0
            val cd21 = -abs(cdelt1) * sign(cdelt2) * sin0
            val cd22 = cdelt2 * cos0
            return arrayOf(doubleArrayOf(cd11, cd12), doubleArrayOf(cd21, cd22))
        }

        @JvmStatic
        private fun pc2cd(
            pc11: Double, pc10: Double,
            pc21: Double, pc22: Double,
            cdelt1: Double, cdelt2: Double,
        ) = arrayOf(
            doubleArrayOf(cdelt1 * pc11, cdelt2 * pc21),
            doubleArrayOf(cdelt1 * pc10, cdelt2 * pc22),
        )

        @JvmStatic
        private fun createProjection(
            wcs: WCSTransform,
            projectionType: ProjectionType,
            cx: Double, cy: Double,
        ): Projection {
            return when (projectionType) {
                ProjectionType.ZPN -> TODO()
                ProjectionType.TPV -> TODO()
                ProjectionType.BON -> TODO()
                ProjectionType.CEA -> TODO()
                ProjectionType.COD -> TODO()
                ProjectionType.COE -> TODO()
                ProjectionType.COO -> TODO()
                ProjectionType.COP -> TODO()
                ProjectionType.SZP -> TODO()
                ProjectionType.NCP -> TODO()
                else -> {
                    createStandardProjectionWithParameters(wcs, projectionType, cx, cy)
                        ?: createStandardProjection(wcs, projectionType, cx, cy)
                        ?: throw NotImplementedError("$projectionType not implemented")
                }
            }
        }

        @JvmStatic
        private fun createStandardProjectionWithParameters(
            wcs: WCSTransform,
            projectionType: ProjectionType,
            cx: Double, cy: Double,
        ): Projection? {
            if (!wcs.header.containsKey("PV2_1")) {
                return null
            }

            val pv21 = wcs.header.getDoubleValue("PV2_1")

            return try {
                if (wcs.header.containsKey("PV2_2")) {
                    val pv22 = wcs.header.getDoubleValue("PV2_2")

                    projectionType.type
                        ?.getConstructor(Double::class.java, Double::class.java, Double::class.java, Double::class.java)
                        ?.newInstance(wcs.header.getDoubleValue("CRVAL1") * cx, wcs.header.getDoubleValue("CRVAL2") * cy, pv21, pv22)
                } else {
                    projectionType.type
                        ?.getConstructor(Double::class.java, Double::class.java, Double::class.java)
                        ?.newInstance(wcs.header.getDoubleValue("CRVAL1") * cx, wcs.header.getDoubleValue("CRVAL2") * cy, pv21)
                }
            } catch (e: NoSuchMethodException) {
                null
            }
        }

        @JvmStatic
        private fun createStandardProjection(
            wcs: WCSTransform,
            projectionType: ProjectionType,
            cx: Double, cy: Double,
        ): Projection? {
            return try {
                projectionType.type
                    ?.getConstructor(Double::class.java, Double::class.java)
                    ?.newInstance(wcs.header.getDoubleValue("CRVAL1") * cx, wcs.header.getDoubleValue("CRVAL2") * cy)
            } catch (e: NoSuchMethodException) {
                null
            }
        }
    }
}
