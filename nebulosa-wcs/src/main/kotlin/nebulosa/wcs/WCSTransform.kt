package nebulosa.wcs

import nebulosa.constants.RAD2DEG
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.PairOfAngle
import nebulosa.wcs.projection.AbstractProjection
import nebulosa.wcs.projection.Projection
import nebulosa.wcs.projection.ProjectionType
import kotlin.math.abs
import kotlin.math.sign

class WCSTransform(@JvmField internal val header: Map<String, Any>) {

    private val cd: DoubleArray
    private val cdi: DoubleArray
    private val projection: Projection

    val lonpole = header.getDoubleValue("LONPOLE")
        ?: header.getDoubleValue("PV1_3")
        ?: Double.NaN

    val latpole = header.getDoubleValue("LATPOLE")
        ?: header.getDoubleValue("PV1_4")
        ?: Double.NaN

    val hasCd = header.containsKey("CD1_1") ||
            header.containsKey("CDELT1") && header.containsKey("CROTA2") ||
            header.containsKey("CDELT1") && header.containsKey("PC1_1")

    val crpix1 = header.getDoubleValue("CRPIX1")!!

    val crpix2 = header.getDoubleValue("CRPIX2")!!

    init {
        val ctype1 = header.getStringValue("CTYPE1")!!
        val projectionType = ProjectionType.valueOf(ctype1.substring(ctype1.lastIndexOf('-') + 1, ctype1.length))
        val cx = header.getStringValue("CUNIT1").convertCunitToDegrees()
        val cy = header.getStringValue("CUNIT2").convertCunitToDegrees()
        projection = createProjection(this, projectionType, cx, cy) as AbstractProjection

        if (projectionType != ProjectionType.TPV) {
            // Native longitude of the fiducial point.
            if (header.containsKey("PV1_1")) {
                projection.phi0 = header.getDoubleValue("PV1_1")!!.rad
            }
            // Native latitude of the celestial pole.
            if (header.containsKey("PV1_2")) {
                projection.theta0 = header.getDoubleValue("PV1_2")!!.rad
            }
        }

        // Native longitude of the celestial pole.
        if (lonpole.isFinite()) projection.phip = lonpole.deg

        // Native latitude of the celestial pole.
        if (latpole.isFinite()) projection.thetap = latpole.deg

        cd = computeCdMatrix()
        cdi = cd.inverseMatrix()
    }

    private fun computeCdMatrix(): DoubleArray {
        return if (hasCd) {
            doubleArrayOf(cd(1, 1), cd(1, 2), cd(2, 1), cd(2, 2))
        } else {
            val a = header.getDoubleValue("CDELT1")!!
            val b = header.getDoubleValue("CDELT2")!!
            val c = header.getDoubleValue("CROTA2")?.deg ?: Angle.ZERO
            computeCdFromCdelt(a, b, c)
        }
    }

    private fun cd(i: Int, j: Int): Double {
        return if (header.containsKey("CD1_1")) {
            header.getDoubleValue("CD${i}_$j")!!
        } else if (header.containsKey("CROTA2")) {
            val a = header.getDoubleValue("CDELT1")!!
            val b = header.getDoubleValue("CDELT2")!!
            val c = header.getDoubleValue("CROTA2")!!.deg
            val cd = computeCdFromCdelt(a, b, c)
            cd[2 * i + j - 3]
        } else if (header.containsKey("PC1_1")) {
            val pc11 = header.getDoubleValue("PC1_1")!!
            val pc12 = header.getDoubleValue("PC1_2")!!
            val pc21 = header.getDoubleValue("PC2_1")!!
            val pc22 = header.getDoubleValue("PC2_2")!!
            val a = header.getDoubleValue("CDELT1")!!
            val b = header.getDoubleValue("CDELT2")!!
            val cd = pc2cd(pc11, pc12, pc21, pc22, a, b)
            cd[2 * i + j - 3]
        } else {
            throw IllegalArgumentException("cd[$i,$j] not found")
        }
    }

    private fun DoubleArray.inverseMatrix(): DoubleArray {
        val det = (cd[0] * cd[3] - cd[1] * cd[2])
        return doubleArrayOf(cd[3] / det, -cd[1] / det, -cd[2] / det, cd[0] / det)
    }

    fun pixelToWorld(x: Double, y: Double): PairOfAngle {
        val cx = x - crpix1
        val cy = y - crpix2
        val v0 = cx * cd[0] + cy * cd[1]
        val v1 = cx * cd[2] + cy * cd[3]
        return projection.computeCelestialSphericalCoordinate(v0, v1)
    }

    fun worldToPixel(rightAscension: Angle, declination: Angle): DoubleArray {
        val coord = projection.computeProjectionPlaneCoordinate(rightAscension, declination)
        val v0 = coord[0] * cdi[0] + coord[1] * cdi[1]
        val v1 = coord[0] * cdi[2] + coord[1] * cdi[3]
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
        private fun computeCdFromCdelt(cdelt1: Double, cdelt2: Double, crota: Angle): DoubleArray {
            val cos0 = crota.cos
            val sin0 = crota.sin
            val cd11 = cdelt1 * cos0
            val cd12 = abs(cdelt2) * sign(cdelt1) * sin0
            val cd21 = -abs(cdelt1) * sign(cdelt2) * sin0
            val cd22 = cdelt2 * cos0
            return doubleArrayOf(cd11, cd12, cd21, cd22)
        }

        @JvmStatic
        private fun pc2cd(
            pc11: Double, pc10: Double,
            pc21: Double, pc22: Double,
            cdelt1: Double, cdelt2: Double,
        ) = doubleArrayOf(cdelt1 * pc11, cdelt2 * pc21, cdelt1 * pc10, cdelt2 * pc22)

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
                        ?.newInstance(wcs.header.getDoubleValue("CRVAL1")!! * cx, wcs.header.getDoubleValue("CRVAL2")!! * cy, pv21, pv22)
                } else {
                    projectionType.type
                        ?.getConstructor(Double::class.java, Double::class.java, Double::class.java)
                        ?.newInstance(wcs.header.getDoubleValue("CRVAL1")!! * cx, wcs.header.getDoubleValue("CRVAL2")!! * cy, pv21)
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
                    ?.newInstance(wcs.header.getDoubleValue("CRVAL1")!! * cx, wcs.header.getDoubleValue("CRVAL2")!! * cy)
            } catch (e: NoSuchMethodException) {
                null
            }
        }

        @JvmStatic
        private fun Map<String, Any>.getStringValue(key: String): String? {
            return this[key]?.toString()
        }

        @JvmStatic
        private fun Map<String, Any>.getDoubleValue(key: String): Double? {
            val value = this[key] ?: return null
            return if (value is String) value.toDoubleOrNull()
            else value as? Double
        }
    }
}
