package nebulosa.erfa

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Matrix3D

data class AstrometryParameters(
    @JvmField val pmt: Double = 0.0, // PM time interval (SSB, Julian years).
    @JvmField val eb: CartesianCoordinate = CartesianCoordinate.ZERO, // SSB to observer (vector, au).
    @JvmField val ehx: Double = 0.0, val ehy: Double = 0.0, val ehz: Double = 0.0, // Sun to observer (unit vector).
    @JvmField val em: Distance = 0.0, // Distance from Sun to observer.
    @JvmField val vx: Double = 0.0, val vy: Double = 0.0, val vz: Double = 0.0, // Barycentric observer velocity (c)
    @JvmField val bm1: Double = 0.0, // sqrt(1-|v|^2): reciprocal of Lorenz factor.
    @JvmField val bpn: Matrix3D = Matrix3D.IDENTITY, // Bias-precession-nutation matrix.
    @JvmField val along: Angle = 0.0, // Longitude + s' + dERA(DUT).
    @JvmField val phi: Angle = 0.0, // Geodetic latitude.
    @JvmField val xpl: Angle = 0.0, // Polar motion xp wrt local meridian.
    @JvmField val ypl: Angle = 0.0, // Polar motion yp wrt local meridian.
    @JvmField val sphi: Double = 0.0, // Sine of geodetic latitude.
    @JvmField val cphi: Double = 0.0, // Cosine of geodetic latitude.
    @JvmField val diurab: Double = 0.0, // Magnitude of diurnal aberration vector.
    @JvmField val eral: Angle = 0.0, // "local" Earth rotation angle.
    @JvmField val refa: Angle = 0.0, // Refraction constant A.
    @JvmField val refb: Angle = 0.0, // Refraction constant B.
)
