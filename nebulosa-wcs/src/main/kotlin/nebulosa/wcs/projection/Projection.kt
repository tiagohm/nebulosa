package nebulosa.wcs.projection

import nebulosa.math.Angle
import nebulosa.math.PairOfAngle

interface Projection {

    val type: ProjectionType

    /**
     * Returns the celestial longitude in radians of the ﬁducial point.
     */
    val crval1: Angle

    /**
     * Returns the celestial latitude in radians of the ﬁducial point.
     */
    val crval2: Angle

    /**
     * Returns the native longitude of the fiducial point.
     */
    val phi0: Angle

    /**
     * Returns the native latitude of the ﬁducial point.
     */
    val theta0: Angle

    /**
     * Returns the native longitude of the celestial pole.
     */
    val phip: Angle

    /**
     * Returns the native latitude of the celestial pole.
     */
    val thetap: Angle

    /**
     * Computes the native spherical coordinates from the projection plane coordinates.
     */
    fun project(x: Double, y: Double): PairOfAngle

    /**
     * Computes the projection plane coordinates from the native spherical coordinates.
     */
    fun unproject(longitude: Angle, latitude: Angle): DoubleArray

    /**
     * Computes the celestial spherical coordinates from the projection plane coordinates.
     */
    fun computeCelestialSphericalCoordinate(x: Double, y: Double): PairOfAngle

    /**
     * Computes the projection plane coordinates from the celestial spherical coordinates.
     */
    fun computeProjectionPlaneCoordinate(rightAscension: Angle, declination: Angle): DoubleArray

    /**
     * Returns true if the given [latitude]/[longitude] point is visible in this projection.
     */
    fun inside(longitude: Angle, latitude: Angle): Boolean

    /**
     * Checks if the line is visible.
     */
    fun isLineToDraw(x0: Double, y0: Double, x1: Double, y1: Double): Boolean
}
