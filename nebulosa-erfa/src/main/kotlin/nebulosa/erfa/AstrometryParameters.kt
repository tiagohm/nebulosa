package nebulosa.erfa

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Matrix3D

data class AstrometryParameters(
    val pmt: Double = 0.0, // PM time interval (SSB, Julian years).
    val ebx: Distance = Distance.ZERO, val eby: Distance = Distance.ZERO, val ebz: Distance = Distance.ZERO, // SSB to observer (vector, au).
    val ehx: Double = 0.0, val ehy: Double = 0.0, val ehz: Double = 0.0, // Sun to observer (unit vector).
    val em: Distance = Distance.ZERO, // Distance from Sun to observer.
    val vx: Double = 0.0, val vy: Double = 0.0, val vz: Double = 0.0, // Barycentric observer velocity (c)
    val bm1: Double = 0.0, // sqrt(1-|v|^2): reciprocal of Lorenz factor.
    val bpn: Matrix3D = Matrix3D.IDENTITY, // Bias-precession-nutation matrix.
    val along: Angle = Angle.ZERO, // Longitude + s' + dERA(DUT).
    val phi: Angle = Angle.ZERO, // Geodetic latitude.
    val xpl: Angle = Angle.ZERO, // Polar motion xp wrt local meridian.
    val ypl: Angle = Angle.ZERO, // Polar motion yp wrt local meridian.
    val sphi: Double = 0.0, // Sine of geodetic latitude.
    val cphi: Double = 0.0, // Cosine of geodetic latitude.
    val diurab: Double = 0.0, // Magnitude of diurnal aberration vector.
    val eral: Angle = Angle.ZERO, // "local" Earth rotation angle.
    val refa: Angle = Angle.ZERO, // Refraction constant A.
    val refb: Angle = Angle.ZERO, // Refraction constant B.
)
