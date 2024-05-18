package nebulosa.alignment.polar.point.three

import nebulosa.constants.PI
import nebulosa.constants.TAU
import nebulosa.math.Angle
import nebulosa.math.Vector3D
import nebulosa.math.cos
import nebulosa.math.sin
import nebulosa.plate.solving.PlateSolution
import nebulosa.time.InstantOfTime
import nebulosa.time.UTC
import kotlin.math.abs
import kotlin.math.hypot

internal data class PolarErrorDetermination(
    @JvmField val initialReferenceFrame: PlateSolution,
    @JvmField val firstPosition: Position,
    @JvmField val secondPosition: Position,
    @JvmField val thirdPosition: Position,
    @JvmField val longitude: Angle,
    @JvmField val latitude: Angle,
) {

    private inline val isNorthern
        get() = latitude > 0.0

    private val plane = with(Vector3D.plane(firstPosition.vector, secondPosition.vector, thirdPosition.vector)) {
        // Flip vector if pointing to the wrong direction.
        if (isNorthern && x < 0 || !isNorthern && x > 0) -normalized else normalized
    }

    @JvmField val initialErrorPosition = Position(plane, longitude, latitude)

    @Volatile var currentReferenceFrame = initialReferenceFrame
        private set

    /**
     * Computes the initial azimuth and altitude errors.
     */
    fun compute(): DoubleArray {
        val altitudeError: Double
        var azimuthError: Double

        val pole = abs(latitude)

        if (isNorthern) {
            altitudeError = initialErrorPosition.topocentric.altitude - pole
            azimuthError = initialErrorPosition.topocentric.azimuth
        } else {
            altitudeError = pole - initialErrorPosition.topocentric.altitude
            azimuthError = initialErrorPosition.topocentric.azimuth + PI
        }

        if (azimuthError > PI) {
            azimuthError -= TAU
        }
        if (azimuthError < -PI) {
            azimuthError += TAU
        }

        return doubleArrayOf(azimuthError, altitudeError)
    }

    /**
     * Computes the updated azimuth and altitude errors from initial errors by [compute].
     */
    fun update(
        time: InstantOfTime,
        initialAzimuthError: Angle, initialAltitudeError: Angle,
        referenceFrame: PlateSolution, compensateRefraction: Boolean = false
    ): DoubleArray {
        currentReferenceFrame = referenceFrame

        val centerX = referenceFrame.widthInPixels / 2.0
        val centerY = referenceFrame.heightInPixels / 2.0

        val originPixel =
            doubleArrayOf(initialReferenceFrame.rightAscension, initialReferenceFrame.declination).stenographicProjection(referenceFrame)
        val pointShift = doubleArrayOf(centerX - originPixel[0], centerY - originPixel[1])

        originPixel[0] += pointShift[0] * 2.0
        originPixel[1] += pointShift[1] * 2.0

        val destinationAltAz = destinationCoordinates(-initialAzimuthError, -initialAltitudeError, time, compensateRefraction)
        val destinationPixel = doubleArrayOf(destinationAltAz[0], destinationAltAz[1]).stenographicProjection(referenceFrame)

        destinationPixel[0] += pointShift[0] * 2.0
        destinationPixel[1] += pointShift[1] * 2.0

        // Azimuth.
        val originalAzimuthAltAz = destinationCoordinates(-initialAzimuthError, 0.0, time, compensateRefraction)
        val originalAzimuthPixel = doubleArrayOf(originalAzimuthAltAz[0], originalAzimuthAltAz[1]).stenographicProjection(referenceFrame)

        originalAzimuthPixel[0] += pointShift[0] * 2.0
        originalAzimuthPixel[1] += pointShift[1] * 2.0

        val lineOriginToAzimuth = Line.fromPoints(originPixel, originalAzimuthPixel)
        val correctedAzimuthLine = Line(lineOriginToAzimuth.slope, centerY - lineOriginToAzimuth.slope * centerX)
        val lineAzimuthToDestination = Line.fromPoints(originalAzimuthPixel, destinationPixel)
        val correctedAzimuthPixel = lineAzimuthToDestination.intersectionWith(correctedAzimuthLine)
        val correctedAzimuthDistance = hypot(correctedAzimuthPixel[0] - centerX, correctedAzimuthPixel[1] - centerY)
        val originalAzimuthDistance = hypot(originalAzimuthPixel[0] - originPixel[0], originalAzimuthPixel[1] - originPixel[1])

        // Altitude.
        val originalAltitudeAltAz = destinationCoordinates(0.0, -initialAltitudeError, time, compensateRefraction)
        val originalAltitudePixel = doubleArrayOf(originalAltitudeAltAz[0], originalAltitudeAltAz[1]).stenographicProjection(referenceFrame)

        originalAltitudePixel[0] += pointShift[0] * 2.0
        originalAltitudePixel[1] += pointShift[1] * 2.0

        val lineOriginToAltitude = Line.fromPoints(originPixel, originalAltitudePixel)
        val correctedAltitudeLine = Line(lineOriginToAltitude.slope, centerY - lineOriginToAltitude.slope * centerX)
        val lineAltitudeToDestination = Line.fromPoints(originalAltitudePixel, destinationPixel)
        val correctedAltitudePixel = lineAltitudeToDestination.intersectionWith(correctedAltitudeLine)
        val correctedAltitudeDistance = hypot(correctedAltitudePixel[0] - centerX, correctedAltitudePixel[1] - centerY)
        val originalAltitudeDistance = hypot(originalAltitudePixel[0] - originPixel[0], originalAltitudePixel[1] - originPixel[1])

        // Check if sign needs to be reversed.

        // Azimuth.
        val originalAzimuthDirection = doubleArrayOf(destinationPixel[0] - originalAltitudePixel[0], destinationPixel[1] - originalAltitudePixel[1])
        val correctedAzimuthDirection =
            doubleArrayOf(destinationPixel[0] - correctedAltitudePixel[0], destinationPixel[1] - correctedAltitudePixel[1])
        // When dot product is positive, the angle between both vectors is smaller than 90°.
        val azimuthSameDirection =
            (originalAzimuthDirection[0] * correctedAzimuthDirection[0] + originalAzimuthDirection[1] * correctedAzimuthDirection[1]) > 0
        val azSign = if (azimuthSameDirection) 1 else -1

        // Altitude.
        val originalAltitudeDirection = doubleArrayOf(destinationPixel[0] - originalAzimuthPixel[0], destinationPixel[1] - originalAzimuthPixel[1])
        val correctedAltitudeDirection = doubleArrayOf(destinationPixel[0] - correctedAzimuthPixel[0], destinationPixel[1] - correctedAzimuthPixel[1])
        // When dot product is positive, the angle between both vectors is smaller than 90°.
        val altitudeSameDirection =
            (originalAltitudeDirection[0] * correctedAltitudeDirection[0] + originalAltitudeDirection[1] * correctedAltitudeDirection[1]) > 0
        val altSign = if (altitudeSameDirection) 1 else -1

        // Error determination.
        val currentAzimuthError = initialAzimuthError * (azSign * correctedAzimuthDistance / originalAzimuthDistance)
        val currentAltitudeError = initialAltitudeError * (altSign * correctedAltitudeDistance / originalAltitudeDistance)

        return doubleArrayOf(currentAzimuthError, currentAltitudeError)
    }

    fun destinationCoordinates(azimuth: Angle, altitude: Angle, time: InstantOfTime = UTC.now(), compensateRefraction: Boolean = false): DoubleArray {
        val (_, _, _, rightAscension, declination) = initialReferenceFrame
        val position = Position(rightAscension, declination, longitude, latitude, time, compensateRefraction)
        // First rotate by azimuth, then from the point at azimuth rotate further by altitude to get to the final position
        val azDest = Vector3D.rotateByRodrigues(position.vector, Vector3D.Z, azimuth)
        val rotatedAltAxis = Vector3D.rotateByRodrigues(Vector3D.Y, Vector3D.Z, azimuth)
        val finalDest = Vector3D.rotateByRodrigues(azDest, rotatedAltAxis, altitude)  // Combination of first az then applied alt.
        return Position(finalDest, longitude, latitude).topocentric.transform(time, compensateRefraction)
    }

    companion object {

        /**
         * Generates a Point with relative X/Y values for centering the current coordinates relative
         * to a given point using steonographic projection.
         */
        @JvmStatic
        internal fun DoubleArray.stenographicProjection(
            solution: PlateSolution,
            centerRA: Angle = solution.rightAscension,
            centerDEC: Angle = solution.declination
        ) = stenographicProjection(
            centerRA, centerDEC,
            solution.widthInPixels / 2.0, solution.heightInPixels / 2.0, solution.scale, solution.orientation
        )

        @JvmStatic
        internal fun DoubleArray.stenographicProjection(
            centerRA: Angle, centerDEC: Angle,
            centerX: Double, centerY: Double, scale: Angle, orientation: Angle
        ): DoubleArray {
            var targetRA = this[0]
            val deltaRA = targetRA - centerRA

            if (deltaRA > PI) {
                targetRA -= TAU
            } else if (deltaRA < -PI) {
                targetRA += TAU
            }

            val targetDEC = this[1]

            val targetDECSin = targetDEC.sin
            val targetDECCos = targetDEC.cos

            val centerDECSin = centerDEC.sin
            val centerDECCos = centerDEC.cos

            val raDiff = targetRA - centerRA
            val raDiffCos = raDiff.cos

            val rotationSin = orientation.sin
            val rotationCos = orientation.cos

            val dd = 2.0 / (1.0 + targetDECSin * centerDECSin + targetDECCos * centerDECCos * raDiffCos)
            val raMod = dd * raDiff.sin * targetDECCos
            val decMod = dd * (targetDECSin * centerDECCos - targetDECCos * centerDECSin * raDiffCos)

            var deltaX = raMod
            var deltaY = decMod

            if (orientation != 0.0) {
                deltaX = raMod * rotationCos + decMod * rotationSin
                deltaY = decMod * rotationCos - raMod * rotationSin
            }

            return doubleArrayOf(centerX - deltaX / scale, centerY - deltaY / scale)
        }
    }
}
