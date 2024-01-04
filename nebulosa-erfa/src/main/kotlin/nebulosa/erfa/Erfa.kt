@file:Suppress("PrivatePropertyName", "NOTHING_TO_INLINE", "FloatingPointLiteralPrecision")

package nebulosa.erfa

import nebulosa.constants.*
import nebulosa.io.lazyBufferedResource
import nebulosa.io.readDoubleArrayLe
import nebulosa.io.readDoubleLe
import nebulosa.math.*
import okio.BufferedSource
import kotlin.math.*
import kotlin.math.PI

/**
 * P-vector to spherical polar coordinates.
 *
 * @return longitude angle (theta), latitude angle (phi) and radial distance.
 */
fun eraP2s(x: Distance, y: Distance, z: Distance): SphericalCoordinate {
    val (theta, phi) = eraC2s(x, y, z)
    val r = sqrt(x * x + y * y + z * z).au
    return SphericalCoordinate(theta, phi, r)
}

/**
 * P-vector to spherical coordinates.
 *
 * @return longitude angle (theta) and latitude angle (phi).
 */
fun eraC2s(x: Distance, y: Distance, z: Distance): PairOfAngle {
    val d2 = x * x + y * y
    val theta = if (d2 == 0.0) 0.0 else atan2(y, x)
    val phi = if (z == 0.0) 0.0 else atan2(z, sqrt(d2))
    return PairOfAngle(theta.rad, phi.rad)
}

/**
 * Apply aberration to transform natural direction into proper direction.
 *
 * @param pnat Natural direction to the source (unit vector).
 * @param v Observer barycentric velocity in units of c.
 * @param s Distance between the Sun and the observer (au).
 * @param bm1 sqrt(1-|v|^2): reciprocal of Lorenz factor.
 *
 * @return Proper direction to source (unit vector).
 */
fun eraAb(pnat: Vector3D, v: Vector3D, s: Distance, bm1: Double): Vector3D {
    val pdv = pnat.dot(v)
    val w1 = 1.0 + pdv / (1.0 + bm1)
    val w2 = SCHWARZSCHILD_RADIUS_OF_THE_SUN / s
    val p = DoubleArray(3)

    for (i in 0..2) {
        p[i] = pnat[i] * bm1 + w1 * v[i] + w2 * (v[i] - pdv * pnat[i])
    }

    return Vector3D(p).normalized
}

/**
 * Transform azimuth and altitude to hour angle and declination.
 *
 * The sign convention for azimuth is north zero, east +pi/2.
 *
 * HA is returned in the range +/-pi. Declination is returned in the range +/-pi/2.
 *
 * The latitude phi is pi/2 minus the angle between the Earth's
 * rotation axis and the adopted zenith.  In many applications it
 * will be sufficient to use the published geodetic latitude of the
 * site.  In very precise (sub-arcsecond) applications, phi can be
 * corrected for polar motion.
 *
 * The azimuth az must be with respect to the rotational north pole,
 * as opposed to the ITRS pole, and an azimuth with respect to north
 * on a map of the Earth's surface will need to be adjusted for
 * polar motion if sub-arcsecond accuracy is required.
 *
 * Should the user wish to work with respect to the astronomical
 * zenith rather than the geodetic zenith, phi will need to be
 * adjusted for deflection of the vertical (often tens of
 * arcseconds), and the zero point of ha will also be affected.
 *
 * @param az azimuth
 * @param alt altitude (informally, elevation)
 * @param phi site latitude
 *
 * @return hour angle (local) and declination
 */
fun eraAe2hd(az: Angle, alt: Angle, phi: Angle): PairOfAngle {
    val sa = az.sin
    val ca = az.cos
    val se = alt.sin
    val ce = alt.cos
    val sp = phi.sin
    val cp = phi.cos

    val x = -ca * ce * sp + se * cp
    val y = -sa * ce
    val z = ca * ce * cp + se * sp

    val r = hypot(x, y)
    val ha = if (r != 0.0) atan2(y, x).rad else 0.0
    val dec = atan2(z, r).rad

    return PairOfAngle(ha, dec)
}

/**
 * Barycentric Coordinate Time, TCB, to Barycentric Dynamical Time, TDB.
 */
fun eraTcbTdb(tcb1: Double, tcb2: Double): DoubleArray {
    val d = tcb1 - (MJD0 + MJD1977)
    val tdb2 = tcb2 + TDB0 / DAYSEC - (d + (tcb2 - TTMINUSTAI / DAYSEC)) * ELB
    return doubleArrayOf(tcb1, tdb2)
}

/**
 * Geocentric Coordinate Time, TCG, to Terrestrial Time, TT.
 */
fun eraTcgTt(tcg1: Double, tcg2: Double): DoubleArray {
    val tt2 = tcg2 - (tcg1 - MJD0 + (tcg2 - (MJD1977 + TTMINUSTAI / DAYSEC))) * ELG
    return doubleArrayOf(tcg1, tt2)
}

/**
 * Barycentric Dynamical Time, TDB, to Barycentric Coordinate Time, TCB.
 */
fun eraTdbTcb(tdb1: Double, tdb2: Double): DoubleArray {
    val d = MJD0 + MJD1977 - tdb1
    val f = tdb2 - TDB0 / DAYSEC
    val tcb2 = f - (d - (f - TTMINUSTAI / DAYSEC)) * (ELB / (1.0 - ELB))
    return doubleArrayOf(tdb1, tcb2)
}

/**
 * Terrestrial Time, TT, to Geocentric Coordinate Time, TCG.
 */
fun eraTtTcg(tt1: Double, tt2: Double): DoubleArray {
    val tcg2 = tt2 + (tt1 - MJD0 + (tt2 - (MJD1977 + TTMINUSTAI / DAYSEC))) * (ELG / (1.0 - ELG))
    return doubleArrayOf(tt1, tcg2)
}

// 787 sets of three coefficients.
// amplitude (microseconds), frequency (radians per Julian millennium since J2000.0), phase (radians).
private val FAIRHEAD by lazyBufferedResource("FAIRHEAD.dat") { readDoubleArrayLe(787 * 3) }

/**
 * An approximation to TDB-TT, the difference between barycentric
 * dynamical time and terrestrial time, for an observer on the Earth.
 *
 * @param tdb1 TDB day (Julian date).
 * @param tdb2 TDB fraction of day (Julian date).
 * @param ut Universal time (UT1, fraction of day).
 * @param elong Longitude (east positive, radians).
 * @param u Distance from Earth spin axis (km).
 * @param v Distance north of equatorial plane (km).
 */
fun eraDtDb(
    tdb1: Double, tdb2: Double,
    ut: Double,
    elong: Angle = 0.0, u: Distance = 0.0, v: Distance = 0.0,
): Double {
    // Time since J2000.0 in Julian millennia.
    val t = (tdb1 - J2000 + tdb2) / DAYSPERJM
    // Convert UT to local solar time in radians.
    val tsol = (ut pmod 1.0) * TAU + elong
    // Combine time argument (millennia) with deg/arcsec factor.
    val w = t / 3600.0
    // Sun Mean Meridian.
    val elsun = (280.46645683 + 1296027711.03429 * w pmod 360.0).deg
    // Sun Mean Anomaly.
    val emsun = (357.52910918 + 1295965810.481 * w pmod 360.0).deg
    // Mean Elongation of Moon from Sun.
    val d = (297.85019547 + 16029616012.090 * w pmod 360.0).deg
    // Mean Longitude of Jupiter.
    val elj = (34.35151874 + 109306899.89453 * w pmod 360.0).deg
    // Mean Longitude of Saturn.
    val els = (50.07744430 + 44046398.47038 * w pmod 360.0).deg
    // TOPOCENTRIC TERMS: Moyer 1981 and Murray 1983.
    val ukm = u.toKilometers
    val vkm = v.toKilometers
    val wt =
        0.00029E-10 * ukm * sin(tsol + elsun - els) + 0.00100E-10 * ukm * sin(tsol - 2.0 * emsun) + 0.00133E-10 * ukm * sin(tsol - d) + 0.00133E-10 * ukm * sin(
            tsol + elsun - elj
        ) - 0.00229E-10 * ukm * sin(tsol + 2.0 * elsun + emsun) - 0.02200E-10 * vkm * cos(elsun + emsun) + 0.05312E-10 * ukm * sin(tsol - emsun) - 0.13677E-10 * ukm * sin(
            tsol + 2.0 * elsun
        ) - 1.31840E-10 * vkm * cos(elsun) + 3.17679E-10 * ukm * sin(tsol)

    val coefficients = FAIRHEAD
    val wn = DoubleArray(5)

    // T^0
    for (j in 1419 downTo 0 step 3) {
        wn[0] += coefficients[j] * sin(coefficients[j + 1] * t + coefficients[j + 2])
    }

    // T^1
    for (j in 2034 downTo 1422 step 3) {
        wn[1] += coefficients[j] * sin(coefficients[j + 1] * t + coefficients[j + 2])
    }

    // T^2
    for (j in 2289 downTo 2037 step 3) {
        wn[2] += coefficients[j] * sin(coefficients[j + 1] * t + coefficients[j + 2])
    }

    // T^3
    for (j in 2349 downTo 2292 step 3) {
        wn[3] += coefficients[j] * sin(coefficients[j + 1] * t + coefficients[j + 2])
    }

    // T^4
    for (j in 2358 downTo 2352 step 3) {
        wn[4] += coefficients[j] * sin(coefficients[j + 1] * t + coefficients[j + 2])
    }

    // Multiply by powers of T and combine.
    val wf = t * (t * (t * (t * wn[4] + wn[3]) + wn[2]) + wn[1]) + wn[0]

    // Adjustments to use JPL planetary masses instead of IAU.
    val wj =
        0.00065E-6 * sin(6069.776754 * t + 4.021194) + 0.00033E-6 * sin(213.299095 * t + 5.543132) + (-0.00196E-6 * sin(6208.294251 * t + 5.696701)) + (-0.00173E-6 * sin(
            74.781599 * t + 2.435900
        )) + 0.03638E-6 * t * t

    // TDB-TT in seconds.
    return wt + wf + wj
}

/**
 * Normalize [angle] into the range -[PI] <= a < +[PI].
 */
fun eraAnpm(angle: Angle): Angle {
    var w = angle % TAU
    if (abs(w) >= PI) w -= if (angle >= 0.0) TAU else -TAU
    return w.rad
}

/**
 * Transform geocentric coordinates to geodetic for a reference
 * ellipsoid of specified form.
 *
 * The [flattening], f, is (for the Earth) a value around 0.00335,
 * i.e. around 1/298.
 */
fun eraGc2Gde(
    radius: Distance, flattening: Double,
    x: Distance, y: Distance, z: Distance,
): SphericalCoordinate {
    val aeps2 = radius * radius * 1e-32
    val e2 = (2.0 - flattening) * flattening

    val e4t = e2 * e2 * 1.5
    val ec2 = 1.0 - e2

    assert(ec2 > 0.0)

    val ec = sqrt(ec2)
    val b = radius * ec

    val p2 = x * x + y * y

    val elong = if (p2 > 0.0) atan2(y, x) else 0.0

    val absz = abs(z)

    val phi: Double
    val height: Double

    // Proceed unless polar case.
    if (p2 > aeps2) {
        // Distance from polar axis.
        val p = sqrt(p2)

        // Normalization.
        val s0 = absz / radius
        val pn = p / radius
        val zc = ec * s0

        // Prepare Newton correction factors.
        val c0 = ec * pn
        val c02 = c0 * c0
        val c03 = c02 * c0
        val s02 = s0 * s0
        val s03 = s02 * s0
        val a02 = c02 + s02
        val a0 = sqrt(a02)
        val a03 = a02 * a0
        val d0 = zc * a03 + e2 * s03
        val f0 = pn * a03 - e2 * c03

        val b0 = e4t * s02 * c02 * pn * (a0 - ec)
        val s1 = d0 * f0 - b0 * s0
        val cc = ec * (f0 * f0 - b0 * c0)

        phi = z.sign * atan(s1 / cc)
        val s12 = s1 * s1
        val cc2 = cc * cc
        height = (p * cc + absz * s1 - radius * sqrt(ec2 * s12 + cc2)) / sqrt(s12 + cc2)
    } else {
        phi = z.sign * PIOVERTWO
        height = absz - b
    }

    return SphericalCoordinate(elong.rad, phi.rad, height.au)
}

/**
 * Transform geodetic coordinates to geocentric using the specified
 * reference ellipsoid.
 *
 * The [flattening], f, is (for the Earth) a value around 0.00335,
 * i.e. around 1/298.
 */
fun eraGd2Gce(
    radius: Distance, flattening: Double,
    elong: Angle, phi: Angle, height: Distance,
): CartesianCoordinate {
    val sp = phi.sin
    val cp = phi.cos
    val w = (1.0 - flattening).let { it * it }
    val d = cp * cp + w * sp * sp

    assert(d > 0.0)

    val ac = radius / sqrt(d)
    val aS = w * ac

    val r = (ac + height) * cp
    val x = r * elong.cos
    val y = r * elong.sin
    val z = (aS + height) * sp

    return CartesianCoordinate(x.au, y.au, z.au)
}

/**
 * Form the celestial to intermediate-frame-of-date matrix given the CIP
 * X,Y and the CIO locator s.
 *
 * @param x Celestial Intermediate Pole
 * @param x Celestial Intermediate Pole
 * @param s The CIO locator s
 */
fun eraC2ixys(
    x: Double, y: Double,
    s: Angle,
): Matrix3D {
    // Obtain the spherical angles E and d.
    val r2 = x * x + y * y
    val e = if (r2 > 0.0) atan2(y, x).rad else 0.0
    val d = atan(sqrt(r2 / (1.0 - r2))).rad

    return Matrix3D.rotZ(e).rotateY(d).rotateZ(-(e + s))
}

/**
 * Earth rotation rate in radians per UT1 second.
 */
const val OM = 1.00273781191135448 * TAU / DAYSEC

/**
 * Form the matrix of polar motion for a given date, IAU 2000.
 *
 * @param xp Coordinates of the pole (radians)
 * @param yp Coordinates of the pole (radians)
 * @param sp The TIO locator s' (radians)
 */
inline fun eraPom00(xp: Angle, yp: Angle, sp: Angle): Matrix3D {
    return Matrix3D.rotZ(sp).rotateY(-xp).rotateX(-yp)
}

/**
 * Position and velocity of a terrestrial observing station.
 *
 * @param elong Longitude (radians, east +ve)
 * @param phi   Latitude (geodetic, radians)
 * @param hm    Height above ref. ellipsoid (geodetic, m)
 * @param xp    Coordinates of the pole (radians)
 * @param yp    Coordinates of the pole (radians)
 * @param sp    The TIO locator s' (radians)
 * @param theta Earth rotation angle (radians)
 *
 * @return Position/velocity vector (m, m/s, CIRS)
 */
fun eraPvtob(
    elong: Angle, phi: Angle, hm: Distance,
    xp: Angle, yp: Angle,
    sp: Angle,
    theta: Angle,
): PositionAndVelocity {
    // Geodetic to geocentric transformation (WGS84).
    val xyzm = eraGd2Gce(6378137.0.m, 1.0 / 298.257223563, elong, phi, hm)

    // Polar motion and TIO position.
    val rpm = eraPom00(xp, yp, sp)
    val (x, y, z) = rpm.transposed * Vector3D(xyzm.x.toMeters, xyzm.y.toMeters, xyzm.z.toMeters)

    val s = theta.sin
    val c = theta.cos

    val px = c * x - s * y
    val py = s * x + c * y

    val vx = OM * (-s * x - c * y)
    val vy = OM * (c * x - s * y)

    return PositionAndVelocity(Vector3D(px, py, z), Vector3D(vx, vy, 0.0))
}

private const val AUDMS = AU_M / DAYSEC
private const val CR = LIGHT_TIME_AU_S / DAYSEC

/**
 * For an observer whose geocentric position and velocity are known,
 * prepare star-independent astrometry parameters for transformations
 * between ICRS and GCRS. The Earth ephemeris is supplied by the
 * caller.
 *
 * @param tdb1  TDB date
 * @param tdb2  TDB fraction date
 * @param px    Observer's geocentric position (m)
 * @param py    Observer's geocentric position (m)
 * @param pz    Observer's geocentric position (m)
 * @param vx    Observer's geocentric velocity (m/s)
 * @param vy    Observer's geocentric velocity (m/s)
 * @param vz    Observer's geocentric velocity (m/s)
 * @param ebpx  Earth barycentric position (au)
 * @param ebpy  Earth barycentric position (au)
 * @param ebpz  Earth barycentric position (au)
 * @param ebvx  Earth barycentric velocity (au/day)
 * @param ebvy  Earth barycentric velocity (au/day)
 * @param ebvz  Earth barycentric velocity (au/day)
 * @param ehpx   Earth heliocentric position (au)
 * @param ehpy   Earth heliocentric position (au)
 * @param ehpz   Earth heliocentric position (au)
 */
@Suppress("UnnecessaryVariable")
fun eraApcs(
    tdb1: Double, tdb2: Double,
    px: Distance, py: Distance, pz: Distance,
    vx: Double, vy: Double, vz: Double,
    ebpx: Distance, ebpy: Distance, ebpz: Distance,
    ebvx: Velocity, ebvy: Velocity, ebvz: Velocity,
    ehpx: Distance, ehpy: Distance, ehpz: Distance,
): AstrometryParameters {
    // Time since reference epoch, years (for proper motion calculation).
    val pmt = (tdb1 - J2000 + tdb2) / DAYSPERJY

    // Adjust Earth ephemeris to observer.
    val dpx = px
    val dvx = vx / AUDMS
    val phx = ehpx + dpx
    val vbx = ebvx + dvx

    val dpy = py
    val dvy = vy / AUDMS
    val vby = ebvy + dvy
    val phy = ehpy + dpy

    val dpz = pz
    val dvz = vz / AUDMS
    val vbz = ebvz + dvz
    val phz = ehpz + dpz

    // Barycentric position of observer (au).
    val pbx = ebpx + dpx
    val pby = ebpy + dpy
    val pbz = ebpz + dpz

    // Heliocentric direction and distance (unit vector and au).
    val ph = Vector3D(phx, phy, phz)
    val em = ph.length
    val (ehx, ehy, ehz) = ph.normalized

    // Barycentric vel. in units of c, and reciprocal of Lorenz factor.
    var v2 = 0.0
    val wx = vbx * CR
    v2 += wx * wx

    val wy = vby * CR
    v2 += wy * wy

    val wz = vbz * CR
    v2 += wz * wz

    val bm1 = sqrt(1.0 - v2)

    return AstrometryParameters(
        pmt = pmt,
        eb = CartesianCoordinate(pbx, pby, pbz),
        em = em.au,
        ehx = ehx, ehy = ehy, ehz = ehz,
        vx = wx, vy = wy, vz = wz,
        bm1 = bm1,
    )
}

/**
 * For a terrestrial observer, prepare star-independent astrometry
 * parameters for transformations between ICRS and observed
 * coordinates.  The caller supplies the Earth ephemeris, the Earth
 * rotation information and the refraction constants as well as the
 * site coordinates.
 *
 * @param tdb1   TDB as a 2-part...
 * @param tdb2   ...Julian Date (Note 1)
 * @param ebpx   Earth barycentric position (au)
 * @param ebpy   Earth barycentric position (au)
 * @param ebpz   Earth barycentric position (au)
 * @param ebvx   Earth barycentric velocity (au/day)
 * @param ebvy   Earth barycentric velocity (au/day)
 * @param ebvz   Earth barycentric velocity (au/day)
 * @param ehpx   Earth heliocentric position (au)
 * @param ehpy   Earth heliocentric position (au)
 * @param ehpz   Earth heliocentric position (au)
 * @param x      CIP X (components of unit vector)
 * @param y      CIP Y (components of unit vector)
 * @param s      The CIO locator s (radians)
 * @param theta  Earth rotation angle (radians)
 * @param elong  Longitude (radians, east +ve)
 * @param phi    Latitude (geodetic, radians)
 * @param hm     Height above ellipsoid (m, geodetic)
 * @param xp     Polar motion coordinates (radians)
 * @param yp     Polar motion coordinates (radians)
 * @param sp     The TIO locator s' (radians)
 * @param refa   Refraction constant A (radians)
 * @param refb   Refraction constant B (radians)
 */
fun eraApco(
    tdb1: Double, tdb2: Double,
    ebpx: Distance, ebpy: Distance, ebpz: Distance,
    ebvx: Velocity, ebvy: Velocity, ebvz: Velocity,
    ehpx: Distance, ehpy: Distance, ehpz: Distance,
    x: Double, y: Double,
    s: Angle,
    theta: Angle, elong: Angle, phi: Angle,
    hm: Distance,
    xp: Angle, yp: Angle,
    sp: Angle,
    refa: Angle, refb: Angle,
): AstrometryParameters {
    // Form the rotation matrix, CIRS to apparent [HA,Dec].
    var r = Matrix3D.rotZ(theta + sp).rotateY(-xp).rotateX(-yp).rotateZ(elong)

    // Solve for local Earth rotation angle.
    val a = r[0, 0]
    val b = r[0, 1]
    val eral = if (a != 0.0 || b != 0.0) atan2(b, a).rad else 0.0

    // Solve for polar motion [X,Y] with respect to local meridian.
    val c = r[0, 2]
    val xpl = atan2(c, hypot(a, b)).rad
    val d = r[1, 2]
    val e = r[2, 2]
    val ypl = if (d != 0.0 || e != 0.0) (-atan2(d, e)).rad else 0.0

    // Adjusted longitude.
    val along = eraAnpm(eral - theta)

    // Functions of latitude.
    val sphi = phi.sin
    val cphi = phi.cos

    // CIO based BPN matrix.
    r = eraC2ixys(x, y, s)

    // Observer's geocentric position and velocity (m, m/s, CIRS).
    val pvc = eraPvtob(elong, phi, hm, xp, yp, sp, theta)

    // Rotate into GCRS.
    val rt = r.transposed
    val p = rt * pvc.position
    val v = rt * pvc.velocity

    // ICRS <-> GCRS parameters.
    return eraApcs(
        tdb1, tdb2,
        p.x.m, p.y.m, p.z.m,
        v.x, v.y, v.z,
        ebpx, ebpy, ebpz,
        ebvx, ebvy, ebvz,
        ehpx, ehpy, ehpz,
    ).copy(
        eral = eral,
        xpl = xpl, ypl = ypl,
        along = along,
        sphi = sphi, cphi = cphi,
        refa = refa, refb = refb,
        diurab = 0.0, // Disable the (redundant) diurnal aberration step.
        bpn = r,
    )
}

/**
 * The TIO locator s', positioning the Terrestrial Intermediate Origin
 * on the equator of the Celestial Intermediate Pole.
 */
fun eraSp00(tt1: Double, tt2: Double): Angle {
    val t = (tt1 - J2000 + tt2) / DAYSPERJC
    val sp = -47e-6 * t
    return sp.arcsec
}

/**
 * Mean obliquity of the ecliptic, IAU 2006 precession model.
 */
fun eraObl06(tt1: Double, tt2: Double): Angle {
    // Interval between fundamental date J2000.0 and given date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC
    // Mean obliquity.
    return (84381.406 + (-46.836769 + (-0.0001831 + (0.00200340 + (-0.000000576 + (-0.0000000434) * t) * t) * t) * t) * t).arcsec
}

/**
 * Precession angles, IAU 2006 (Fukushima-Williams 4-angle formulation).
 */
fun eraPfw06(tt1: Double, tt2: Double): FukushimaWilliamsFourAngles {
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    val gamb = (-0.052928 + (10.556378 + (0.4932044 + (-0.00031238 + (-0.000002788 + (0.0000000260) * t) * t) * t) * t) * t).arcsec
    val phib = (84381.412819 + (-46.811016 + (0.0511268 + (0.00053289 + (-0.000000440 + (-0.0000000176) * t) * t) * t) * t) * t).arcsec
    val psib = (-0.041775 + (5038.481484 + (1.5584175 + (-0.00018522 + (-0.000026452 + (-0.0000000148) * t) * t) * t) * t) * t).arcsec
    val epsa = eraObl06(tt1, tt2)

    return FukushimaWilliamsFourAngles(gamb, phib, psib, epsa)
}

/**
 * Fundamental argument, IERS Conventions (2003): mean anomaly of the Moon.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFal03(t: Double): Angle {
    return (485868.249036 + t * (1717915923.2178 + t * (31.8792 + t * (0.051635 + t * (-0.00024470))))).mod(TURNAS).arcsec
}

/**
 * Fundamental argument, IERS Conventions (2003): mean anomaly of the Sun.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFalp03(t: Double): Angle {
    return (1287104.793048 + t * (129596581.0481 + t * (-0.5532 + t * (0.000136 + t * (-0.00001149))))).mod(TURNAS).arcsec
}

/**
 * Fundamental argument, IERS Conventions (2003): mean anomaly of the Sun.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFad03(t: Double): Angle {
    return (1072260.703692 + t * (1602961601.2090 + t * (-6.3706 + t * (0.006593 + t * (-0.00003169))))).mod(TURNAS).arcsec
}

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of the Moon
 * minus mean longitude of the ascending node.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFaf03(t: Double): Angle {
    return (335779.526232 + t * (1739527262.8478 + t * (-12.7512 + t * (-0.001037 + t * (0.00000417))))).mod(TURNAS).arcsec
}

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of the Moon's ascending node.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFaom03(t: Double): Angle {
    return (450160.398036 + t * (-6962890.5431 + t * (7.4722 + t * (0.007702 + t * (-0.00005939))))).mod(TURNAS).arcsec
}

/**
 * Fundamental argument, IERS Conventions (2003): general accumulated precession in longitude.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFapa03(t: Double) = ((0.024381750 + 0.00000538691 * t) * t).rad

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of Mercury.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFame03(t: Double) = (4.402608842 + 2608.7903141574 * t).mod(TAU).rad

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of Venus.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFave03(t: Double) = (3.176146697 + 1021.3285546211 * t).mod(TAU).rad

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of Earth.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFae03(t: Double) = (1.753470314 + 628.3075849991 * t).mod(TAU).rad

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of Mars.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFama03(t: Double) = (6.203480913 + 334.0612426700 * t).mod(TAU).rad

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of Jupiter.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFaju03(t: Double) = (0.599546497 + 52.9690962641 * t).mod(TAU).rad

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of Saturn.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFasa03(t: Double) = (0.874016757 + 21.3299104960 * t).mod(TAU).rad

/**
 * Fundamental argument, IERS Conventions (2003): mean longitude of Uranus.
 *
 * @param t TDB, Julian centuries since J2000.0 (Note 1)
 */
fun eraFaur03(t: Double) = (5.481293872 + 7.4781598567 * t).mod(TAU).rad

private data class LuniSolarNut(
    @JvmField val nl: Int, @JvmField val nlp: Int, @JvmField val nf: Int,
    @JvmField val nd: Int, @JvmField val nom: Int, @JvmField val sp: Double,
    @JvmField val spt: Double, @JvmField val cp: Double, @JvmField val ce: Double,
    @JvmField val cet: Double, @JvmField val se: Double,
) {

    companion object {

        @JvmStatic
        fun from(source: BufferedSource) = LuniSolarNut(
            source.readIntLe(), source.readIntLe(), source.readIntLe(), source.readIntLe(), source.readIntLe(),
            source.readDoubleLe(), source.readDoubleLe(), source.readDoubleLe(),
            source.readDoubleLe(), source.readDoubleLe(), source.readDoubleLe(),
        )
    }
}

private data class PlanetaryNut(
    @JvmField val nl: Int, val nf: Int, val nd: Int, val nom: Int,
    @JvmField val nme: Int, val nve: Int, val nea: Int, val nma: Int,
    @JvmField val nju: Int, val nsa: Int, val nur: Int, val nne: Int,
    @JvmField val npa: Int, val sp: Int, val cp: Int,
    @JvmField val se: Int, val ce: Int,
) {

    companion object {

        @JvmStatic
        fun from(source: BufferedSource) = PlanetaryNut(
            source.readIntLe(), source.readIntLe(), source.readIntLe(), source.readIntLe(), source.readIntLe(),
            source.readIntLe(), source.readIntLe(), source.readIntLe(), source.readIntLe(),
            source.readIntLe(), source.readIntLe(), source.readIntLe(), source.readIntLe(),
            source.readIntLe(), source.readIntLe(), source.readIntLe(), source.readIntLe(),
        )
    }
}

private val XLS by lazyBufferedResource("LUNISOLAR-NUT.dat") { Array(678) { LuniSolarNut.from(this) } }
private val XPL by lazyBufferedResource("PLANETARY-NUT.dat") { Array(687) { PlanetaryNut.from(this) } }

/**
 * Nutation, IAU 2000A model (MHB2000 luni-solar and planetary nutation
 * with free core nutation omitted).
 */
fun eraNut00a(tt1: Double, tt2: Double): PairOfAngle {
    // Interval between fundamental date J2000.0 and given date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    // Mean anomaly of the Moon (IERS 2003).
    val el = eraFal03(t)

    // Mean anomaly of the Sun (MHB2000).
    val elp = (1287104.79305 + t * (129596581.0481 + t * (-0.5532 + t * (0.000136 + t * (-0.00001149))))).mod(TURNAS).arcsec

    // Mean longitude of the Moon minus that of the ascending node (IERS 2003).
    val f = eraFaf03(t)

    // Mean elongation of the Moon from the Sun (MHB2000).

    val d = (1072260.70369 + t * (1602961601.2090 + t * (-6.3706 + t * (0.006593 + t * (-0.00003169))))).mod(TURNAS).arcsec

    // Mean longitude of the ascending node of the Moon (IERS 2003).
    val om = eraFaom03(t)

    var dp = 0.0
    var de = 0.0

    val xls = XLS

    // Summation of luni-solar nutation series (in reverse order).
    for (i in xls.indices.reversed()) {
        val arg = (xls[i].nl * el + xls[i].nlp * elp + xls[i].nf * f + xls[i].nd * d + xls[i].nom * om).mod(TAU)

        val sarg = sin(arg)
        val carg = cos(arg)

        dp += (xls[i].sp + xls[i].spt * t) * sarg + xls[i].cp * carg
        de += (xls[i].ce + xls[i].cet * t) * carg + xls[i].se * sarg
    }

    val dpls = dp
    val dels = de

    // Mean anomaly of the Moon (MHB2000).
    val al = (2.35555598 + 8328.6914269554 * t).mod(TAU)

    // Mean longitude of the Moon minus that of the ascending node.
    val af = (1.627905234 + 8433.466158131 * t).mod(TAU)

    // Mean elongation of the Moon from the Sun (MHB2000).
    val ad = (5.198466741 + 7771.3771468121 * t).mod(TAU)

    // Mean longitude of the ascending node of the Moon (MHB2000).
    val aom = (2.18243920 - 33.757045 * t).mod(TAU)

    // General accumulated precession in longitude (IERS 2003).
    val apa = eraFapa03(t)

    // Planetary longitudes, Mercury through Uranus (IERS 2003).
    val alme = eraFame03(t)
    val alve = eraFave03(t)
    val alea = eraFae03(t)
    val alma = eraFama03(t)
    val alju = eraFaju03(t)
    val alsa = eraFasa03(t)
    val alur = eraFaur03(t)

    // Neptune longitude (MHB2000).
    val alne = (5.321159000 + 3.8127774000 * t).mod(TAU)

    dp = 0.0
    de = 0.0

    val xpl = XPL

    for (i in xpl.indices.reversed()) {
        val arg = (xpl[i].nl * al + xpl[i].nf * af + xpl[i].nd * ad + xpl[i].nom * aom + xpl[i].nme * alme +
                xpl[i].nve * alve + xpl[i].nea * alea + xpl[i].nma * alma + xpl[i].nju * alju +
                xpl[i].nsa * alsa + xpl[i].nur * alur + xpl[i].nne * alne + xpl[i].npa * apa).mod(TAU)

        val sarg = sin(arg)
        val carg = cos(arg)

        dp += xpl[i].sp * sarg + xpl[i].cp * carg
        de += xpl[i].se * sarg + xpl[i].ce * carg
    }

    val dpp = dp
    val dep = de

    // Units of 0.1 microarcsecond to radians.
    return PairOfAngle((dpls + dpp).arcsec / 10000000.0, (dels + dep).arcsec / 10000000.0)
}

/**
 * IAU 2000A nutation with adjustments to match the IAU 2006 precession.
 */
fun eraNut06a(tt1: Double, tt2: Double): PairOfAngle {
    // Interval between fundamental date J2000.0 and given date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    // Factor correcting for secular variation of J2.
    val fj2 = -2.7774e-6 * t

    // Obtain IAU 2000A nutation.
    val (dp, de) = eraNut00a(tt1, tt2)

    // Apply P03 adjustments (Wallace & Capitaine, 2006, Eqs.5).
    val dpsi = dp + dp * (0.4697e-6 + fj2)
    val deps = de + de * fj2

    return PairOfAngle(dpsi.rad, deps.rad)
}

/**
 * Form rotation matrix given the Fukushima-Williams angles.
 */
inline fun eraFw2m(gamb: Angle, phib: Angle, psi: Angle, eps: Angle): Matrix3D {
    return Matrix3D.rotZ(gamb).rotateX(phib).rotateZ(-psi).rotateX(-eps)
}

/**
 * Form the matrix of precession-nutation for a given date (including
 * frame bias), equinox based, IAU 2006 precession and IAU 2000A
 * nutation models.
 */
fun eraPnm06a(tt1: Double, tt2: Double): Matrix3D {
    // Fukushima-Williams angles for frame bias and precession.
    val (gamb, phib, psib, epsa) = eraPfw06(tt1, tt2)
    // Nutation.
    val (dp, de) = eraNut06a(tt1, tt2)
    // Equinox based nutation x precession x bias matrix.
    return eraFw2m(gamb, phib, psib + dp, epsa + de)
}

private data class Term(
    @JvmField val nfa: IntArray,
    @JvmField val s: Double,
    @JvmField val c: Double,
)

// Polynomial coefficients
private val SP = doubleArrayOf(94.00e-6, 3808.65e-6, -122.68e-6, -72574.11e-6, 27.98e-6, 15.62e-6)

// Terms of order t^0
private val S0 = arrayOf(
    // 1-10
    Term(intArrayOf(0, 0, 0, 0, 1, 0, 0, 0), -2640.73e-6, 0.39e-6),
    Term(intArrayOf(0, 0, 0, 0, 2, 0, 0, 0), -63.53e-6, 0.02e-6),
    Term(intArrayOf(0, 0, 2, -2, 3, 0, 0, 0), -11.75e-6, -0.01e-6),
    Term(intArrayOf(0, 0, 2, -2, 1, 0, 0, 0), -11.21e-6, -0.01e-6),
    Term(intArrayOf(0, 0, 2, -2, 2, 0, 0, 0), 4.57e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 2, 0, 3, 0, 0, 0), -2.02e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 2, 0, 1, 0, 0, 0), -1.98e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 0, 0, 3, 0, 0, 0), 1.72e-6, 0.00e-6),
    Term(intArrayOf(0, 1, 0, 0, 1, 0, 0, 0), 1.41e-6, 0.01e-6),
    Term(intArrayOf(0, 1, 0, 0, -1, 0, 0, 0), 1.26e-6, 0.01e-6),
    // 11-20
    Term(intArrayOf(1, 0, 0, 0, -1, 0, 0, 0), 0.63e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 0, 0, 1, 0, 0, 0), 0.63e-6, 0.00e-6),
    Term(intArrayOf(0, 1, 2, -2, 3, 0, 0, 0), -0.46e-6, 0.00e-6),
    Term(intArrayOf(0, 1, 2, -2, 1, 0, 0, 0), -0.45e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 4, -4, 4, 0, 0, 0), -0.36e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 1, -1, 1, -8, 12, 0), 0.24e-6, 0.12e-6),
    Term(intArrayOf(0, 0, 2, 0, 0, 0, 0, 0), -0.32e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 2, 0, 2, 0, 0, 0), -0.28e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 2, 0, 3, 0, 0, 0), -0.27e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 2, 0, 1, 0, 0, 0), -0.26e-6, 0.00e-6),
    // 21-30
    Term(intArrayOf(0, 0, 2, -2, 0, 0, 0, 0), 0.21e-6, 0.00e-6),
    Term(intArrayOf(0, 1, -2, 2, -3, 0, 0, 0), -0.19e-6, 0.00e-6),
    Term(intArrayOf(0, 1, -2, 2, -1, 0, 0, 0), -0.18e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 0, 0, 0, 8, -13, -1), 0.10e-6, -0.05e-6),
    Term(intArrayOf(0, 0, 0, 2, 0, 0, 0, 0), -0.15e-6, 0.00e-6),
    Term(intArrayOf(2, 0, -2, 0, -1, 0, 0, 0), 0.14e-6, 0.00e-6),
    Term(intArrayOf(0, 1, 2, -2, 2, 0, 0, 0), 0.14e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 0, -2, 1, 0, 0, 0), -0.14e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 0, -2, -1, 0, 0, 0), -0.14e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 4, -2, 4, 0, 0, 0), -0.13e-6, 0.00e-6),
    // 31-33
    Term(intArrayOf(0, 0, 2, -2, 4, 0, 0, 0), 0.11e-6, 0.00e-6),
    Term(intArrayOf(1, 0, -2, 0, -3, 0, 0, 0), -0.11e-6, 0.00e-6),
    Term(intArrayOf(1, 0, -2, 0, -1, 0, 0, 0), -0.11e-6, 0.00e-6),
)

// Terms of order t^1
private val S1 = arrayOf(
    // 1 - 3
    Term(intArrayOf(0, 0, 0, 0, 2, 0, 0, 0), -0.07e-6, 3.57e-6),
    Term(intArrayOf(0, 0, 0, 0, 1, 0, 0, 0), 1.73e-6, -0.03e-6),
    Term(intArrayOf(0, 0, 2, -2, 3, 0, 0, 0), 0.00e-6, 0.48e-6),
)

// Terms of order t^2
private val S2 = arrayOf(
    // 1-10
    Term(intArrayOf(0, 0, 0, 0, 1, 0, 0, 0), 743.52e-6, -0.17e-6),
    Term(intArrayOf(0, 0, 2, -2, 2, 0, 0, 0), 56.91e-6, 0.06e-6),
    Term(intArrayOf(0, 0, 2, 0, 2, 0, 0, 0), 9.84e-6, -0.01e-6),
    Term(intArrayOf(0, 0, 0, 0, 2, 0, 0, 0), -8.85e-6, 0.01e-6),
    Term(intArrayOf(0, 1, 0, 0, 0, 0, 0, 0), -6.38e-6, -0.05e-6),
    Term(intArrayOf(1, 0, 0, 0, 0, 0, 0, 0), -3.07e-6, 0.00e-6),
    Term(intArrayOf(0, 1, 2, -2, 2, 0, 0, 0), 2.23e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 2, 0, 1, 0, 0, 0), 1.67e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 2, 0, 2, 0, 0, 0), 1.30e-6, 0.00e-6),
    Term(intArrayOf(0, 1, -2, 2, -2, 0, 0, 0), 0.93e-6, 0.00e-6),
    // 11-20
    Term(intArrayOf(1, 0, 0, -2, 0, 0, 0, 0), 0.68e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 2, -2, 1, 0, 0, 0), -0.55e-6, 0.00e-6),
    Term(intArrayOf(1, 0, -2, 0, -2, 0, 0, 0), 0.53e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 0, 2, 0, 0, 0, 0), -0.27e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 0, 0, 1, 0, 0, 0), -0.27e-6, 0.00e-6),
    Term(intArrayOf(1, 0, -2, -2, -2, 0, 0, 0), -0.26e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 0, 0, -1, 0, 0, 0), -0.25e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 2, 0, 1, 0, 0, 0), 0.22e-6, 0.00e-6),
    Term(intArrayOf(2, 0, 0, -2, 0, 0, 0, 0), -0.21e-6, 0.00e-6),
    Term(intArrayOf(2, 0, -2, 0, -1, 0, 0, 0), 0.20e-6, 0.00e-6),
    // 21-25
    Term(intArrayOf(0, 0, 2, 2, 2, 0, 0, 0), 0.17e-6, 0.00e-6),
    Term(intArrayOf(2, 0, 2, 0, 2, 0, 0, 0), 0.13e-6, 0.00e-6),
    Term(intArrayOf(2, 0, 0, 0, 0, 0, 0, 0), -0.13e-6, 0.00e-6),
    Term(intArrayOf(1, 0, 2, -2, 2, 0, 0, 0), -0.12e-6, 0.00e-6),
    Term(intArrayOf(0, 0, 2, 0, 0, 0, 0, 0), -0.11e-6, 0.00e-6),
)

// Terms of order t^3
private val S3 = arrayOf(
    // 1-4
    Term(intArrayOf(0, 0, 0, 0, 1, 0, 0, 0), 0.30e-6, -23.42e-6),
    Term(intArrayOf(0, 0, 2, -2, 2, 0, 0, 0), -0.03e-6, -1.46e-6),
    Term(intArrayOf(0, 0, 2, 0, 2, 0, 0, 0), -0.01e-6, -0.25e-6),
    Term(intArrayOf(0, 0, 0, 0, 2, 0, 0, 0), 0.00e-6, 0.23e-6),
)

// Terms of order t^4
private val S4 = arrayOf(
    // 1-1
    Term(intArrayOf(0, 0, 0, 0, 1, 0, 0, 0), -0.26e-6, -0.01e-6),
)

private val S = arrayOf(S0, S1, S2, S3, S4)

/**
 * The CIO locator s, positioning the Celestial Intermediate Origin on
 * the equator of the Celestial Intermediate Pole, given the CIP's X,Y
 * coordinates. Compatible with IAU 2006/2000A precession-nutation.
 */
fun eraS06(tt1: Double, tt2: Double, x: Double, y: Double): Angle {
    // Interval between fundamental epoch J2000.0 and current date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    // Fundamental Arguments (from IERS Conventions 2003)
    val fa = DoubleArray(8)

    // Mean anomaly of the Moon.
    fa[0] = eraFal03(t)
    // Mean anomaly of the Sun.
    fa[1] = eraFalp03(t)
    // Mean longitude of the Moon minus that of the ascending node.
    fa[2] = eraFaf03(t)
    // Mean elongation of the Moon from the Sun.
    fa[3] = eraFad03(t)
    // Mean longitude of the ascending node of the Moon.
    fa[4] = eraFaom03(t)
    // Mean longitude of Venus.
    fa[5] = eraFave03(t)
    // Mean longitude of Earth.
    fa[6] = eraFae03(t)
    // General precession in longitude.
    fa[7] = eraFapa03(t)

    // Evalutate s.
    val w = DoubleArray(6) { SP[it] }

    for (k in S.indices) {
        for (i in S[k].indices.reversed()) {
            var a = 0.0

            for (j in 0..7) {
                a += S[k][i].nfa[j] * fa[j]
            }

            w[k] += S[k][i].s * sin(a) + S[k][i].c * cos(a)
        }
    }

    return (w[0] + (w[1] + (w[2] + (w[3] + (w[4] + w[5] * t) * t) * t) * t) * t).arcsec - x * y / 2.0
}

/**
 * The CIO locator s, positioning the Celestial Intermediate Origin on
 * the equator of the Celestial Intermediate Pole, using the IAU 2006
 * precession and IAU 2000A nutation models.
 *
 * The CIO locator s is the difference between the right ascensions
 * of the same point in two systems. The two systems are the GCRS
 * and the CIP,CIO, and the point is the ascending node of the
 * CIP equator. The CIO locator s remains a small fraction of
 * 1 arcsecond throughout 1900-2100.
 *
 * The series used to compute s is in fact for s+XY/2, where X and Y
 * are the x and y components of the CIP unit vector; this series is
 * more compact than a direct series for s would be. The present
 * function uses the full IAU 2000A nutation model when predicting
 * the CIP position.
 */
fun eraS06a(tt1: Double, tt2: Double): Angle {
    // Bias-precession-nutation-matrix, IAU 20006/2000A.
    val rnpb = eraPnm06a(tt1, tt2)
    // Extract the CIP coordinates.
    val x = rnpb[2, 0]
    val y = rnpb[2, 1]
    // Compute the CIO locator s, given the CIP coordinates.
    return eraS06(tt1, tt2, x, y)
}

/**
 * Earth rotation angle (IAU 2000 model).
 */
fun era00(ut11: Double, ut12: Double): Angle {
    val t = ut11 + (ut12 - J2000)
    return (TAU * (ut12 + 0.7790572732640 + 0.00273781191135448 * t)).pmod(TAU).rad
}

/**
 * Determine the constants A and B in the atmospheric refraction model
 * dZ = A tan Z + B tan^3 Z.
 *
 * Z is the "observed" zenith distance (i.e. affected by refraction)
 * and dZ is what to add to Z to give the "topocentric" (i.e. in vacuo)
 * zenith distance.
 *
 * @param phpa Pressure at the observer (hPa = millibar)
 * @param tc   Ambient temperature at the observer (deg C)
 * @param rh   Relative humidity at the observer (range 0-1)
 * @param wl   Wavelength (micrometers)
 *
 * @return tan Z coefficient (radians) and tan^3 Z coefficient (radians)
 */
fun eraRefco(phpa: Double, tc: Double, rh: Double, wl: Double): PairOfAngle {
    // Decide whether optical/IR or radio case:  switch at 100 microns.
    val optic = wl <= 100.0

    val t = max(-150.0, min(tc, 200.0))
    val p = max(0.0, min(phpa, 10000.0))
    val r = max(0.0, min(rh, 1.0))
    val w = max(0.1, min(wl, 1e+6))

    // Water vapour pressure at the observer.
    val pw = if (p > 0.0) {
        val ps = 10.0.pow((0.7859 + 0.03477 * t) / (1.0 + 0.00412 * t)) * (1.0 + p * (4.5e-6 + 6e-10 * t * t))
        r * ps / (1.0 - (1.0 - r) * ps / p)
    } else {
        0.0
    }

    // Refractive index minus 1 at the observer.

    val tk = t + 273.15

    val gamma = if (optic) {
        val wlsq = w * w
        ((77.53484e-6 + (4.39108e-7 + 3.666e-9 / wlsq) / wlsq) * p - 11.2684e-6 * pw) / tk
    } else {
        (77.6890e-6 * p - (6.3938e-6 - 0.375463 / tk) * pw) / tk
    }

    // Formula for beta from Stone, with empirical adjustments.
    var beta = 4.4474e-6 * tk
    if (!optic) beta -= 0.0074 * pw * beta

    // Refraction constants from Green.
    val refa = gamma * (1.0 - beta)
    val refb = -gamma * (beta - gamma / 2.0)

    return PairOfAngle(refa.rad, refb.rad)
}

//fun apco(frame: CoordinateFrame) {
//    val location = (frame as? AltAz)?.location ?: throw IllegalArgumentException("Frame must be AltAz")
//    val (longitude, latitude, height) = location.geodetic()
//    val time = frame.time!! // TODO: time is TT
//    val (xp, yp) = frame.polarMotion.pmXY(time)
//    val sp = eraSp00(time.whole, time.fraction)
//    val (x, y, s) = cip(time) // eraS06
//    val (ut11, ut12) = time // TODO: .ut1
//    val era = era00(ut11, ut12)
//
//    val ref = if (frame.pressure > 0.0) eraRefco(frame.pressure, frame.temperature, frame.relativeHumidity, frame.obswl)
//    else 0.0 to 0.0
//
//    // eraApco(time.whole, time.fraction)
//}

/**
 * For a geocentric observer, prepare star-independent astrometry
 * parameters for transformations between ICRS and GCRS coordinates.
 * The Earth ephemeris is supplied by the caller.
 *
 * @param tdb1  TDB date
 * @param tdb2  TDB fraction date
 * @param ebpx  Earth barycentric position (au)
 * @param ebpy  Earth barycentric position (au)
 * @param ebpz  Earth barycentric position (au)
 * @param ebvx  Earth barycentric velocity (au/day)
 * @param ebvy  Earth barycentric velocity (au/day)
 * @param ebvz  Earth barycentric velocity (au/day)
 * @param ehpx  Earth heliocentric position (au)
 * @param ehpy  Earth heliocentric position (au)
 * @param ehpz  Earth heliocentric position (au)
 */
fun eraApcg(
    tdb1: Double, tdb2: Double,
    ebpx: Distance, ebpy: Distance, ebpz: Distance,
    ebvx: Velocity, ebvy: Velocity, ebvz: Velocity,
    ehpx: Distance, ehpy: Distance, ehpz: Distance,
): AstrometryParameters {
    return eraApcs(
        tdb1, tdb2,
        0.0, 0.0, 0.0,
        0.0, 0.0, 0.0,
        ebpx, ebpy, ebpz,
        ebvx, ebvy, ebvz,
        ehpx, ehpy, ehpz,
    )
}

private const val AM12 = 0.000000211284
private const val AM13 = -0.000000091603
private const val AM21 = -0.000000230286
private const val AM22 = 0.917482137087
private const val AM23 = -0.397776982902
private const val AM32 = 0.397776982902
private const val AM33 = 0.917482137087

private val E0X by lazyBufferedResource("E0X.dat") { readDoubleArrayLe(1503) }
private val E0Y by lazyBufferedResource("E0Y.dat") { readDoubleArrayLe(1503) }
private val E0Z by lazyBufferedResource("E0Z.dat") { readDoubleArrayLe(411) }
private val E1X by lazyBufferedResource("E1X.dat") { readDoubleArrayLe(237) }
private val E1Y by lazyBufferedResource("E1Y.dat") { readDoubleArrayLe(240) }
private val E1Z by lazyBufferedResource("E1Z.dat") { readDoubleArrayLe(36) }
private val E2X by lazyBufferedResource("E2X.dat") { readDoubleArrayLe(15) }
private val E2Y by lazyBufferedResource("E2Y.dat") { readDoubleArrayLe(15) }
private val E2Z by lazyBufferedResource("E2Z.dat") { readDoubleArrayLe(9) }

private val S0X by lazyBufferedResource("S0X.dat") { readDoubleArrayLe(636) }
private val S0Y by lazyBufferedResource("S0Y.dat") { readDoubleArrayLe(639) }
private val S0Z by lazyBufferedResource("S0Z.dat") { readDoubleArrayLe(207) }
private val S1X by lazyBufferedResource("S1X.dat") { readDoubleArrayLe(150) }
private val S1Y by lazyBufferedResource("S1Y.dat") { readDoubleArrayLe(150) }
private val S1Z by lazyBufferedResource("S1Z.dat") { readDoubleArrayLe(42) }
private val S2X by lazyBufferedResource("S2X.dat") { readDoubleArrayLe(27) }
private val S2Y by lazyBufferedResource("S2Y.dat") { readDoubleArrayLe(27) }
private val S2Z by lazyBufferedResource("S2Z.dat") { readDoubleArrayLe(6) }

private val CE0 by lazy { arrayOf(E0X, E0Y, E0Z) }
private val CE1 by lazy { arrayOf(E1X, E1Y, E1Z) }
private val CE2 by lazy { arrayOf(E2X, E2Y, E2Z) }
private val CS0 by lazy { arrayOf(S0X, S0Y, S0Z) }
private val CS1 by lazy { arrayOf(S1X, S1Y, S1Z) }
private val CS2 by lazy { arrayOf(S2X, S2Y, S2Z) }

/**
 * Earth position and velocity, heliocentric and barycentric, with
 * respect to the Barycentric Celestial Reference System.
 *
 * @return Heliocentric Earth position/velocity (au, au/day) and
 * Barycentric Earth position/velocity (au, au/day).
 */
fun eraEpv00(tdb1: Double, tdb2: Double): Pair<PositionAndVelocity, PositionAndVelocity> {
    val t = (tdb1 - J2000 + tdb2) / DAYSPERJY
    val t2 = t * t

    val ph = DoubleArray(3)
    val vh = DoubleArray(3)
    val pb = DoubleArray(3)
    val vb = DoubleArray(3)

    val ce0 = CE0
    val ce1 = CE1
    val ce2 = CE2
    val cs0 = CS0
    val cs1 = CS1
    val cs2 = CS2

    for (i in 0..2) {
        var xyz = 0.0
        var xyzd = 0.0

        // Sun to Earth, T^0 terms.
        for (k in ce0[i].indices step 3) {
            val a = ce0[i][k]
            val b = ce0[i][k + 1]
            val c = ce0[i][k + 2]
            val p = b + c * t
            xyz += a * cos(p)
            xyzd -= a * c * sin(p)
        }

        // Sun to Earth, T^1 terms.
        for (k in ce1[i].indices step 3) {
            val a = ce1[i][k]
            val b = ce1[i][k + 1]
            val c = ce1[i][k + 2]
            val ct = c * t
            val p = b + ct
            val cp = cos(p)
            xyz += a * t * cp
            xyzd += a * (cp - ct * sin(p))
        }

        // Sun to Earth, T^2 terms.
        for (k in ce2[i].indices step 3) {
            val a = ce2[i][k]
            val b = ce2[i][k + 1]
            val c = ce2[i][k + 2]
            val ct = c * t
            val p = b + ct
            val cp = cos(p)
            xyz += a * t2 * cp
            xyzd += a * t * (2.0 * cp - ct * sin(p))
        }

        // Heliocentric Earth position and velocity component.
        ph[i] = xyz
        vh[i] = xyzd / DAYSPERJY

        // SSB to Sun, T^0 terms.
        for (k in cs0[i].indices step 3) {
            val a = cs0[i][k]
            val b = cs0[i][k + 1]
            val c = cs0[i][k + 2]
            val p = b + c * t
            xyz += a * cos(p)
            xyzd -= a * c * sin(p)
        }

        // SSB to Sun, T^1 terms.
        for (k in cs1[i].indices step 3) {
            val a = cs1[i][k]
            val b = cs1[i][k + 1]
            val c = cs1[i][k + 2]
            val ct = c * t
            val p = b + ct
            val cp = cos(p)
            xyz += a * t * cp
            xyzd += a * (cp - ct * sin(p))
        }

        // SSB to Sun, T^2 terms.
        for (k in cs2[i].indices step 3) {
            val a = cs2[i][k]
            val b = cs2[i][k + 1]
            val c = cs2[i][k + 2]
            val ct = c * t
            val p = b + ct
            val cp = cos(p)
            xyz += a * t2 * cp
            xyzd += a * t * (2.0 * cp - ct * sin(p))
        }

        // Barycentric Earth position and velocity component.
        pb[i] = xyz
        vb[i] = xyzd / DAYSPERJY
    }

    val phx = ph[0] + AM12 * ph[1] + AM13 * ph[2]
    val phy = AM21 * ph[0] + AM22 * ph[1] + AM23 * ph[2]
    val phz = AM32 * ph[1] + AM33 * ph[2]

    val vhx = vh[0] + AM12 * vh[1] + AM13 * vh[2]
    val vhy = AM21 * vh[0] + AM22 * vh[1] + AM23 * vh[2]
    val vhz = AM32 * vh[1] + AM33 * vh[2]

    val pbx = pb[0] + AM12 * pb[1] + AM13 * pb[2]
    val pby = AM21 * pb[0] + AM22 * pb[1] + AM23 * pb[2]
    val pbz = AM32 * pb[1] + AM33 * pb[2]

    val vbx = vb[0] + AM12 * vb[1] + AM13 * vb[2]
    val vby = AM21 * vb[0] + AM22 * vb[1] + AM23 * vb[2]
    val vbz = AM32 * vb[1] + AM33 * vb[2]

    return PositionAndVelocity(Vector3D(phx, phy, phz), Vector3D(vhx, vhy, vhz)) to
            PositionAndVelocity(Vector3D(pbx, pby, pbz), Vector3D(vbx, vby, vbz))
}

/**
 * For a geocentric observer, prepare star-independent astrometry
 * parameters for transformations between ICRS and GCRS coordinates.
 * The caller supplies the date, and ERFA models are used to predict
 * the Earth ephemeris.
 *
 * The parameters produced by this function are required in the
 * parallax, light deflection and aberration parts of the astrometric
 * transformation chain.
 */
fun eraApcg13(tdb1: Double, tdb2: Double): AstrometryParameters {
    val (h, b) = eraEpv00(tdb1, tdb2)

    return eraApcg(
        tdb1, tdb2,
        b.position.x.au, b.position.y.au, b.position.z.au,
        b.velocity.x.auDay, b.velocity.y.auDay, b.velocity.z.auDay,
        h.position.x.au, h.position.y.au, h.position.z.au,
    )
}

private data class ComplementaryTerm(
    @JvmField val nfa: IntArray,
    @JvmField val s: Double,
    @JvmField val c: Double,
)

// Terms of order t^0
private val E0 = arrayOf(
    // 1-10
    ComplementaryTerm(intArrayOf(0, 0, 0, 0, 1, 0, 0, 0), 2640.96e-6, -0.39e-6),
    ComplementaryTerm(intArrayOf(0, 0, 0, 0, 2, 0, 0, 0), 63.52e-6, -0.02e-6),
    ComplementaryTerm(intArrayOf(0, 0, 2, -2, 3, 0, 0, 0), 11.75e-6, 0.01e-6),
    ComplementaryTerm(intArrayOf(0, 0, 2, -2, 1, 0, 0, 0), 11.21e-6, 0.01e-6),
    ComplementaryTerm(intArrayOf(0, 0, 2, -2, 2, 0, 0, 0), -4.55e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 0, 2, 0, 3, 0, 0, 0), 2.02e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 0, 2, 0, 1, 0, 0, 0), 1.98e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 0, 0, 0, 3, 0, 0, 0), -1.72e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 1, 0, 0, 1, 0, 0, 0), -1.41e-6, -0.01e-6),
    ComplementaryTerm(intArrayOf(0, 1, 0, 0, -1, 0, 0, 0), -1.26e-6, -0.01e-6),

    // 11-20
    ComplementaryTerm(intArrayOf(1, 0, 0, 0, -1, 0, 0, 0), -0.63e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(1, 0, 0, 0, 1, 0, 0, 0), -0.63e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 1, 2, -2, 3, 0, 0, 0), 0.46e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 1, 2, -2, 1, 0, 0, 0), 0.45e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 0, 4, -4, 4, 0, 0, 0), 0.36e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 0, 1, -1, 1, -8, 12, 0), -0.24e-6, -0.12e-6),
    ComplementaryTerm(intArrayOf(0, 0, 2, 0, 0, 0, 0, 0), 0.32e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 0, 2, 0, 2, 0, 0, 0), 0.28e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(1, 0, 2, 0, 3, 0, 0, 0), 0.27e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(1, 0, 2, 0, 1, 0, 0, 0), 0.26e-6, 0.00e-6),

    // 21-30
    ComplementaryTerm(intArrayOf(0, 0, 2, -2, 0, 0, 0, 0), -0.21e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 1, -2, 2, -3, 0, 0, 0), 0.19e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 1, -2, 2, -1, 0, 0, 0), 0.18e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 0, 0, 0, 0, 8, -13, -1), -0.10e-6, 0.05e-6),
    ComplementaryTerm(intArrayOf(0, 0, 0, 2, 0, 0, 0, 0), 0.15e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(2, 0, -2, 0, -1, 0, 0, 0), -0.14e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(1, 0, 0, -2, 1, 0, 0, 0), 0.14e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 1, 2, -2, 2, 0, 0, 0), -0.14e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(1, 0, 0, -2, -1, 0, 0, 0), 0.14e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(0, 0, 4, -2, 4, 0, 0, 0), 0.13e-6, 0.00e-6),

    // 31-33
    ComplementaryTerm(intArrayOf(0, 0, 2, -2, 4, 0, 0, 0), -0.11e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(1, 0, -2, 0, -3, 0, 0, 0), 0.11e-6, 0.00e-6),
    ComplementaryTerm(intArrayOf(1, 0, -2, 0, -1, 0, 0, 0), 0.11e-6, 0.00e-6),
)

// Terms of order t^1
private val E1 = arrayOf(
    ComplementaryTerm(intArrayOf(0, 0, 0, 0, 1, 0, 0, 0), -0.87e-6, 0.00e-6),
)

/**
 * Equation of the equinoxes complementary terms, consistent with
 * IAU 2000 resolutions.
 * The "complementary terms" are part of the equation of the
 * equinoxes (EE), classically the difference between apparent and
 * mean Sidereal Time:
 *
 *    GAST = GMST + EE
 *
 * with:
 *
 *    EE = dpsi * cos(eps)
 *
 * where dpsi is the nutation in longitude and eps is the obliquity
 * of date.  However, if the rotation of the Earth were constant in
 * an inertial frame the classical formulation would lead to
 * apparent irregularities in the UT1 timescale traceable to side-
 * effects of precession-nutation.  In order to eliminate these
 * effects from UT1, "complementary terms" were introduced in 1994
 * (IAU, 1994) and took effect from 1997 (Capitaine and Gontier,
 * 1993):
 *
 *    GAST = GMST + CT + EE
 *
 * By convention, the complementary terms are included as part of
 * the equation of the equinoxes rather than as part of the mean
 * Sidereal Time.  This slightly compromises the "geometrical"
 * interpretation of mean sidereal time but is otherwise
 * inconsequential.
 *
 * The present function computes CT in the above expression,
 * compatible with IAU 2000 resolutions (Capitaine et al., 2002, and
 * IERS Conventions 2003).
 */
fun eraEect00(tt1: Double, tt2: Double): Angle {
    // Interval between fundamental epoch J2000.0 and current date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    // Fundamental Arguments (from IERS Conventions 2003)
    val fa = DoubleArray(14)

    // Mean anomaly of the Moon.
    fa[0] = eraFal03(t)

    // Mean anomaly of the Sun.
    fa[1] = eraFalp03(t)

    // Mean longitude of the Moon minus that of the ascending node.
    fa[2] = eraFaf03(t)

    // Mean elongation of the Moon from the Sun.
    fa[3] = eraFad03(t)

    // Mean longitude of the ascending node of the Moon.
    fa[4] = eraFaom03(t)

    // Mean longitude of Venus.
    fa[5] = eraFave03(t)

    // Mean longitude of Earth.
    fa[6] = eraFae03(t)

    // General precession in longitude.
    fa[7] = eraFapa03(t)

    var s0 = 0.0
    var s1 = 0.0

    for (i in E0.indices.reversed()) {
        var a = 0.0

        for (j in 0..7) {
            a += E0[i].nfa[j] * fa[j]
        }

        s0 += E0[i].s * sin(a) + E0[i].c * cos(a)
    }

    for (i in E1.indices.reversed()) {
        var a = 0.0

        for (j in 0..7) {
            a += E1[i].nfa[j] * fa[j]
        }

        s1 += E1[i].s * sin(a) + E1[i].c * cos(a)
    }

    return (s0 + s1 * t).arcsec
}

/**
 * Earth rotation angle (IAU 2000 model).
 */
fun eraEra00(ut11: Double, ut12: Double): Angle {
    val t = ut12 + (ut11 - J2000)

    // Fractional part of T (days).
    val f = ut12 % 1.0 + ut11 % 1.0

    // Earth rotation angle at this UT1.
    return eraAnpm(TAU * (f + 0.7790572732640 + 0.00273781191135448 * t))
}

/**
 * The equation of the equinoxes, compatible with IAU 2000 resolutions,
 * given the nutation in longitude and the mean obliquity.
 *
 *  The result, which is in radians, operates in the following sense:
 *
 *     Greenwich apparent ST = GMST + equation of the equinoxes
 *
 *  The result is compatible with the IAU 2000 resolutions.  For
 *  further details, see IERS Conventions 2003 and Capitaine et al.
 *  (2002).
 *
 * @param tt1  TT day (Julian date).
 * @param tt2  TT fraction of day (Julian date).
 * @param epsa Mean obliquity (radians).
 * @param dpsi Nutation in longitude.
 *
 * @return equation of the equinoxes in radians.
 *
 */
inline fun eraEe00(tt1: Double, tt2: Double, epsa: Angle, dpsi: Angle): Angle {
    return dpsi * epsa.cos + eraEect00(tt1, tt2)
}

/**
 * Greenwich mean sidereal time (model consistent with IAU 2000 resolutions).
 *
 * Both UT1 and TT are required, UT1 to predict the Earth rotation
 * and TT to predict the effects of precession. If UT1 is used for
 * both purposes, errors of order 100 microarcseconds result.
 *
 * This GMST is compatible with the IAU 2000 resolutions and must be
 * used only in conjunction with other IAU 2000 compatible
 * components such as precession-nutation and equation of the
 * equinoxes.
 *
 * @param ut11  UT1 day (Julian date).
 * @param ut12  UT1 fraction of day (Julian date).
 * @param tt1   TT day (Julian date).
 * @param tt2   TT fraction of day (Julian date).
 */
fun eraGmst00(ut11: Double, ut12: Double, tt1: Double, tt2: Double): Angle {
    // TT Julian centuries since J2000.0.
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    // Greenwich Mean Sidereal Time, IAU 2000.
    return (eraEra00(ut11, ut12) + (0.014506 + (4612.15739966 + (1.39667721 + (-0.00009344 + 0.00001882 * t) * t) * t) * t).arcsec).normalized
}


/**
 * Greenwich mean sidereal time (model consistent with IAU 2006 precession).
 *
 * Both UT1 and TT are required, UT1 to predict the Earth rotation
 * and TT to predict the effects of precession. If UT1 is used for
 * both purposes, errors of order 100 microarcseconds result.
 *
 * This GMST is compatible with the IAU 2006 precession and must not
 * be used with other precession models.
 *
 * @param ut11  UT1 day (Julian date).
 * @param ut12  UT1 fraction of day (Julian date).
 * @param tt1   TT day (Julian date).
 * @param tt2   TT fraction of day (Julian date).
 */
fun eraGmst06(ut11: Double, ut12: Double, tt1: Double, tt2: Double): Angle {
    // TT Julian centuries since J2000.0.
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    // Greenwich Mean Sidereal Time, IAU 2006.
    return (eraEra00(ut11, ut12) +
            (0.014506 + (4612.156534 + (1.3915817 + (-0.00000044 + (-0.000029956 + (-0.0000000368) * t) * t) * t) * t) * t).arcsec).normalized
}

/**
 * Equation of the equinoxes, compatible with IAU 2000 resolutions and
 * IAU 2006/2000A precession-nutation.
 *
 * @param tt1   TT day (Julian date).
 * @param tt2   TT fraction of day (Julian date).
 */
fun eraEe06a(tt1: Double, tt2: Double): Angle {
    return eraAnpm(eraGst06a(0.0, 0.0, tt1, tt2) - eraGmst06(0.0, 0.0, tt1, tt2))
}

/**
 * Greenwich apparent sidereal time (consistent with IAU 2000 and 2006 resolutions).
 *
 * Both UT1 and TT are required, UT1 to predict the Earth rotation
 * and TT to predict the effects of precession-nutation. If UT1 is
 * used for both purposes, errors of order 100 microarcseconds
 * result.
 *
 * This GAST is compatible with the IAU 2000/2006 resolutions and
 * must be used only in conjunction with IAU 2006 precession and
 * IAU 2000A nutation.
 *
 * @param ut11  UT1 day (Julian date).
 * @param ut12  UT1 fraction of day (Julian date).
 * @param tt1   TT day (Julian date).
 * @param tt2   TT fraction of day (Julian date).
 */
fun eraGst06a(ut11: Double, ut12: Double, tt1: Double, tt2: Double): Angle {
    val rnpb = eraPnm06a(tt1, tt2)
    return eraGst06(ut11, ut12, tt1, tt2, rnpb)
}

/**
 * Greenwich apparent sidereal time, IAU 2006, given the NPB matrix.
 *
 * Both UT1 and TT are required, UT1 to predict the Earth rotation
 * and TT to predict the effects of precession-nutation. If UT1 is
 * used for both purposes, errors of order 100 microarcseconds
 * result.
 *
 * Although the function uses the IAU 2006 series for s+XY/2, it is
 * otherwise independent of the precession-nutation model and can in
 * practice be used with any equinox-based NPB matrix.
 *
 * @param ut11  UT1 day (Julian date).
 * @param ut12  UT1 fraction of day (Julian date).
 * @param tt1   TT day (Julian date).
 * @param tt2   TT fraction of day (Julian date).
 * @param rnpb  Nutation x precession x bias matrix.
 */
fun eraGst06(ut11: Double, ut12: Double, tt1: Double, tt2: Double, rnpb: Matrix3D): Angle {
    val x = rnpb[2, 0]
    val y = rnpb[2, 1]

    // The CIO locator, s.
    val s = eraS06(tt1, tt2, x, y)

    // Greenwich apparent sidereal time.
    val era = eraEra00(ut11, ut12)
    val eors = eraEors(rnpb, s)

    return (era - eors).normalized
}

/**
 * Equation of the origins, given the classical NPB matrix and the quantity [s].
 *
 * @param rnpb nutation x precession x bias matrix.
 * @param s    CIO locator.
 *
 * @return The equation of the origins in radians.
 */
fun eraEors(rnpb: Matrix3D, s: Angle): Angle {
    val x = rnpb[2, 0]
    val ax = x / (1.0 + rnpb[2, 2])
    val xs = 1.0 - ax * x
    val ys = -ax * rnpb[2, 1]
    val zs = -x
    val p = rnpb[0, 0] * xs + rnpb[0, 1] * ys + rnpb[0, 2] * zs
    val q = rnpb[1, 0] * xs + rnpb[1, 1] * ys + rnpb[1, 2] * zs
    return if (p != 0.0 || q != 0.0) s - atan2(q, p).rad else s
}

/**
 * Greenwich apparent sidereal time (consistent with IAU 2000 resolutions).
 */
fun eraGst00a(ut11: Double, ut12: Double, tt1: Double, tt2: Double): Angle {
    return (eraGmst00(ut11, ut12, tt1, tt2) + eraEe00a(tt1, tt2)).normalized
}

/**
 * Greenwich apparent sidereal time (consistent with IAU 2000
 * resolutions but using the truncated nutation model IAU 2000B).
 */
fun eraGst00b(ut11: Double, ut12: Double): Angle {
    return (eraGmst00(ut11, ut12, ut11, ut12) + eraEe00b(ut11, ut12)).normalized
}

/**
 * Equation of the equinoxes, compatible with IAU 2000 resolutions.
 */
fun eraEe00a(tt1: Double, tt2: Double): Angle {
    // IAU 2000 precession-rate adjustments.
    val (_, depspr) = eraPr00(tt1, tt2)
    // Mean obliquity, consistent with IAU 2000 precession-nutation.
    val epsa = eraObl80(tt1, tt2) + depspr
    // Nutation in longitude.
    val (dpsi) = eraNut00a(tt1, tt2)
    // Equation of the equinoxes.
    return eraEe00(tt1, tt2, epsa, dpsi)
}

/**
 * Equation of the equinoxes, compatible with IAU 2000 resolutions but
 * using the truncated nutation model IAU 2000B.
 */
fun eraEe00b(tt1: Double, tt2: Double): Angle {
    // IAU 2000 precession-rate adjustments.
    val (_, depspr) = eraPr00(tt1, tt2)
    // Mean obliquity, consistent with IAU 2000 precession-nutation.
    val epsa = eraObl80(tt1, tt2) + depspr
    // Nutation in longitude.
    val (dpsi) = eraNut00b(tt1, tt2)
    // Equation of the equinoxes.
    return eraEe00(tt1, tt2, epsa, dpsi)
}

// Precession and obliquity corrections (radians per century).
private const val PRECOR = -0.29965 * ASEC2RAD
private const val OBLCOR = -0.02524 * ASEC2RAD

/**
 * Precession-rate part of the IAU 2000 precession-nutation models (part of MHB2000).
 *
 * The precession adjustments are expressed as "nutation
 * components", corrections in longitude and obliquity with respect
 * to the J2000.0 equinox and ecliptic.
 *
 * Although the precession adjustments are stated to be with respect
 * to Lieske et al. (1977), the MHB2000 model does not specify which
 * set of Euler angles are to be used and how the adjustments are to
 * be applied.  The most literal and straightforward procedure is to
 * adopt the 4-rotation epsilon_0, psi_A, omega_A, xi_A option, and
 * to add dpsipr to psi_A and depspr to both omega_A and eps_A.
 *
 * This is an implementation of one aspect of the IAU 2000A nutation
 * model, formally adopted by the IAU General Assembly in 2000,
 * namely MHB2000 (Mathews et al. 2002).
 */
fun eraPr00(tt1: Double, tt2: Double): PairOfAngle {
    // Interval between fundamental epoch J2000.0 and given date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC
    return PairOfAngle(PRECOR * t, OBLCOR * t)
}

/**
 * Mean obliquity of the ecliptic, IAU 1980 model.
 */
fun eraObl80(tt1: Double, tt2: Double): Angle {
    // Interval between fundamental date J2000.0 and given date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC
    // Mean obliquity.
    return (84381.448 + (-46.8150 + (-0.00059 + 0.001813 * t) * t) * t).arcsec
}

private val X = arrayOf(
    // 1-10
    LuniSolarNut(0, 0, 0, 0, 1, -172064161.0, -174666.0, 33386.0, 92052331.0, 9086.0, 15377.0),
    LuniSolarNut(0, 0, 2, -2, 2, -13170906.0, -1675.0, -13696.0, 5730336.0, -3015.0, -4587.0),
    LuniSolarNut(0, 0, 2, 0, 2, -2276413.0, -234.0, 2796.0, 978459.0, -485.0, 1374.0),
    LuniSolarNut(0, 0, 0, 0, 2, 2074554.0, 207.0, -698.0, -897492.0, 470.0, -291.0),
    LuniSolarNut(0, 1, 0, 0, 0, 1475877.0, -3633.0, 11817.0, 73871.0, -184.0, -1924.0),
    LuniSolarNut(0, 1, 2, -2, 2, -516821.0, 1226.0, -524.0, 224386.0, -677.0, -174.0),
    LuniSolarNut(1, 0, 0, 0, 0, 711159.0, 73.0, -872.0, -6750.0, 0.0, 358.0),
    LuniSolarNut(0, 0, 2, 0, 1, -387298.0, -367.0, 380.0, 200728.0, 18.0, 318.0),
    LuniSolarNut(1, 0, 2, 0, 2, -301461.0, -36.0, 816.0, 129025.0, -63.0, 367.0),
    LuniSolarNut(0, -1, 2, -2, 2, 215829.0, -494.0, 111.0, -95929.0, 299.0, 132.0),
    // 11-20
    LuniSolarNut(0, 0, 2, -2, 1, 128227.0, 137.0, 181.0, -68982.0, -9.0, 39.0),
    LuniSolarNut(-1, 0, 2, 0, 2, 123457.0, 11.0, 19.0, -53311.0, 32.0, -4.0),
    LuniSolarNut(-1, 0, 0, 2, 0, 156994.0, 10.0, -168.0, -1235.0, 0.0, 82.0),
    LuniSolarNut(1, 0, 0, 0, 1, 63110.0, 63.0, 27.0, -33228.0, 0.0, -9.0),
    LuniSolarNut(-1, 0, 0, 0, 1, -57976.0, -63.0, -189.0, 31429.0, 0.0, -75.0),
    LuniSolarNut(-1, 0, 2, 2, 2, -59641.0, -11.0, 149.0, 25543.0, -11.0, 66.0),
    LuniSolarNut(1, 0, 2, 0, 1, -51613.0, -42.0, 129.0, 26366.0, 0.0, 78.0),
    LuniSolarNut(-2, 0, 2, 0, 1, 45893.0, 50.0, 31.0, -24236.0, -10.0, 20.0),
    LuniSolarNut(0, 0, 0, 2, 0, 63384.0, 11.0, -150.0, -1220.0, 0.0, 29.0),
    LuniSolarNut(0, 0, 2, 2, 2, -38571.0, -1.0, 158.0, 16452.0, -11.0, 68.0),
    // 21-30
    LuniSolarNut(0, -2, 2, -2, 2, 32481.0, 0.0, 0.0, -13870.0, 0.0, 0.0),
    LuniSolarNut(-2, 0, 0, 2, 0, -47722.0, 0.0, -18.0, 477.0, 0.0, -25.0),
    LuniSolarNut(2, 0, 2, 0, 2, -31046.0, -1.0, 131.0, 13238.0, -11.0, 59.0),
    LuniSolarNut(1, 0, 2, -2, 2, 28593.0, 0.0, -1.0, -12338.0, 10.0, -3.0),
    LuniSolarNut(-1, 0, 2, 0, 1, 20441.0, 21.0, 10.0, -10758.0, 0.0, -3.0),
    LuniSolarNut(2, 0, 0, 0, 0, 29243.0, 0.0, -74.0, -609.0, 0.0, 13.0),
    LuniSolarNut(0, 0, 2, 0, 0, 25887.0, 0.0, -66.0, -550.0, 0.0, 11.0),
    LuniSolarNut(0, 1, 0, 0, 1, -14053.0, -25.0, 79.0, 8551.0, -2.0, -45.0),
    LuniSolarNut(-1, 0, 0, 2, 1, 15164.0, 10.0, 11.0, -8001.0, 0.0, -1.0),
    LuniSolarNut(0, 2, 2, -2, 2, -15794.0, 72.0, -16.0, 6850.0, -42.0, -5.0),
    // 31-40
    LuniSolarNut(0, 0, -2, 2, 0, 21783.0, 0.0, 13.0, -167.0, 0.0, 13.0),
    LuniSolarNut(1, 0, 0, -2, 1, -12873.0, -10.0, -37.0, 6953.0, 0.0, -14.0),
    LuniSolarNut(0, -1, 0, 0, 1, -12654.0, 11.0, 63.0, 6415.0, 0.0, 26.0),
    LuniSolarNut(-1, 0, 2, 2, 1, -10204.0, 0.0, 25.0, 5222.0, 0.0, 15.0),
    LuniSolarNut(0, 2, 0, 0, 0, 16707.0, -85.0, -10.0, 168.0, -1.0, 10.0),
    LuniSolarNut(1, 0, 2, 2, 2, -7691.0, 0.0, 44.0, 3268.0, 0.0, 19.0),
    LuniSolarNut(-2, 0, 2, 0, 0, -11024.0, 0.0, -14.0, 104.0, 0.0, 2.0),
    LuniSolarNut(0, 1, 2, 0, 2, 7566.0, -21.0, -11.0, -3250.0, 0.0, -5.0),
    LuniSolarNut(0, 0, 2, 2, 1, -6637.0, -11.0, 25.0, 3353.0, 0.0, 14.0),
    LuniSolarNut(0, -1, 2, 0, 2, -7141.0, 21.0, 8.0, 3070.0, 0.0, 4.0),
    // 41-50
    LuniSolarNut(0, 0, 0, 2, 1, -6302.0, -11.0, 2.0, 3272.0, 0.0, 4.0),
    LuniSolarNut(1, 0, 2, -2, 1, 5800.0, 10.0, 2.0, -3045.0, 0.0, -1.0),
    LuniSolarNut(2, 0, 2, -2, 2, 6443.0, 0.0, -7.0, -2768.0, 0.0, -4.0),
    LuniSolarNut(-2, 0, 0, 2, 1, -5774.0, -11.0, -15.0, 3041.0, 0.0, -5.0),
    LuniSolarNut(2, 0, 2, 0, 1, -5350.0, 0.0, 21.0, 2695.0, 0.0, 12.0),
    LuniSolarNut(0, -1, 2, -2, 1, -4752.0, -11.0, -3.0, 2719.0, 0.0, -3.0),
    LuniSolarNut(0, 0, 0, -2, 1, -4940.0, -11.0, -21.0, 2720.0, 0.0, -9.0),
    LuniSolarNut(-1, -1, 0, 2, 0, 7350.0, 0.0, -8.0, -51.0, 0.0, 4.0),
    LuniSolarNut(2, 0, 0, -2, 1, 4065.0, 0.0, 6.0, -2206.0, 0.0, 1.0),
    LuniSolarNut(1, 0, 0, 2, 0, 6579.0, 0.0, -24.0, -199.0, 0.0, 2.0),
    // 51-60
    LuniSolarNut(0, 1, 2, -2, 1, 3579.0, 0.0, 5.0, -1900.0, 0.0, 1.0),
    LuniSolarNut(1, -1, 0, 0, 0, 4725.0, 0.0, -6.0, -41.0, 0.0, 3.0),
    LuniSolarNut(-2, 0, 2, 0, 2, -3075.0, 0.0, -2.0, 1313.0, 0.0, -1.0),
    LuniSolarNut(3, 0, 2, 0, 2, -2904.0, 0.0, 15.0, 1233.0, 0.0, 7.0),
    LuniSolarNut(0, -1, 0, 2, 0, 4348.0, 0.0, -10.0, -81.0, 0.0, 2.0),
    LuniSolarNut(1, -1, 2, 0, 2, -2878.0, 0.0, 8.0, 1232.0, 0.0, 4.0),
    LuniSolarNut(0, 0, 0, 1, 0, -4230.0, 0.0, 5.0, -20.0, 0.0, -2.0),
    LuniSolarNut(-1, -1, 2, 2, 2, -2819.0, 0.0, 7.0, 1207.0, 0.0, 3.0),
    LuniSolarNut(-1, 0, 2, 0, 0, -4056.0, 0.0, 5.0, 40.0, 0.0, -2.0),
    LuniSolarNut(0, -1, 2, 2, 2, -2647.0, 0.0, 11.0, 1129.0, 0.0, 5.0),
    // 61-70
    LuniSolarNut(-2, 0, 0, 0, 1, -2294.0, 0.0, -10.0, 1266.0, 0.0, -4.0),
    LuniSolarNut(1, 1, 2, 0, 2, 2481.0, 0.0, -7.0, -1062.0, 0.0, -3.0),
    LuniSolarNut(2, 0, 0, 0, 1, 2179.0, 0.0, -2.0, -1129.0, 0.0, -2.0),
    LuniSolarNut(-1, 1, 0, 1, 0, 3276.0, 0.0, 1.0, -9.0, 0.0, 0.0),
    LuniSolarNut(1, 1, 0, 0, 0, -3389.0, 0.0, 5.0, 35.0, 0.0, -2.0),
    LuniSolarNut(1, 0, 2, 0, 0, 3339.0, 0.0, -13.0, -107.0, 0.0, 1.0),
    LuniSolarNut(-1, 0, 2, -2, 1, -1987.0, 0.0, -6.0, 1073.0, 0.0, -2.0),
    LuniSolarNut(1, 0, 0, 0, 2, -1981.0, 0.0, 0.0, 854.0, 0.0, 0.0),
    LuniSolarNut(-1, 0, 0, 1, 0, 4026.0, 0.0, -353.0, -553.0, 0.0, -139.0),
    LuniSolarNut(0, 0, 2, 1, 2, 1660.0, 0.0, -5.0, -710.0, 0.0, -2.0),
    // 71-77
    LuniSolarNut(-1, 0, 2, 4, 2, -1521.0, 0.0, 9.0, 647.0, 0.0, 4.0),
    LuniSolarNut(-1, 1, 0, 1, 1, 1314.0, 0.0, 0.0, -700.0, 0.0, 0.0),
    LuniSolarNut(0, -2, 2, -2, 1, -1283.0, 0.0, 0.0, 672.0, 0.0, 0.0),
    LuniSolarNut(1, 0, 2, 2, 1, -1331.0, 0.0, 8.0, 663.0, 0.0, 4.0),
    LuniSolarNut(-2, 0, 2, 2, 2, 1383.0, 0.0, -2.0, -594.0, 0.0, -2.0),
    LuniSolarNut(-1, 0, 0, 0, 2, 1405.0, 0.0, 4.0, -610.0, 0.0, 2.0),
    LuniSolarNut(1, 1, 2, -2, 2, 1290.0, 0.0, 0.0, -556.0, 0.0, 0.0),
)

private const val DPPLAN = -0.135 * MILLIASEC2RAD
private const val DEPLAN = 0.388 * MILLIASEC2RAD

/**
 * Nutation, IAU 2000B model.
 */
fun eraNut00b(tt1: Double, tt2: Double): PairOfAngle {
    // Interval between fundamental epoch J2000.0 and given date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    // Fundamental (Delaunay) arguments from Simon et al. (1994)

    // Mean anomaly of the Moon.
    val el = (485868.249036 + 1717915923.2178 * t).mod(TURNAS).arcsec
    // Mean anomaly of the Sun.
    val elp = (1287104.79305 + 129596581.0481 * t).mod(TURNAS).arcsec
    // Mean argument of the latitude of the Moon.
    val f = (335779.526232 + 1739527262.8478 * t).mod(TURNAS).arcsec
    // Mean elongation of the Moon from the Sun.
    val d = (1072260.70369 + 1602961601.2090 * t).mod(TURNAS).arcsec
    // Mean longitude of the ascending node of the Moon.
    val om = (450160.398036 - 6962890.5431 * t).mod(TURNAS).arcsec

    var dp = 0.0
    var de = 0.0

    for (i in X.indices.reversed()) {
        // Argument and functions.
        val arg = (X[i].nl * el + X[i].nlp * elp + X[i].nf * f + X[i].nd * d + X[i].nom * om).mod(TAU)

        val sarg = sin(arg)
        val carg = cos(arg)

        // Term.
        dp += (X[i].sp + X[i].spt * t) * sarg + X[i].cp * carg
        de += (X[i].ce + X[i].cet * t) * carg + X[i].se * sarg
    }

    return PairOfAngle(dp.arcsec / 10000000.0 + DPPLAN, de.arcsec / 10000000.0 + DEPLAN)
}

/**
 * Assemble the celestial to terrestrial matrix from CIO-based
 * components (the celestial-to-intermediate matrix, the Earth Rotation
 * Angle and the polar motion matrix).
 *
 * @param rc2i double[3][3]    Celestial-to-intermediate matrix.
 * @param era  double          Earth rotation angle (radians).
 * @param rpom double[3][3]    Polar-motion matrix.
 */
inline fun eraC2tcio(rc2i: Matrix3D, era: Angle, rpom: Matrix3D): Matrix3D {
    return rpom * rc2i.rotateZ(era)
}

/**
 * ICRS equatorial to ecliptic rotation matrix, IAU 2006.
 */
fun eraEcm06(tt1: Double, tt2: Double): Matrix3D {
    // Obliquity, IAU 2006.
    val ob = eraObl06(tt1, tt2)
    // Precession-bias matrix, IAU 2006.
    val bp = eraPmat06(tt1, tt2)
    // Equatorial of date to ecliptic matrix.
    val e = Matrix3D.rotX(ob)
    // ICRS to ecliptic coordinates rotation matrix, IAU 2006.
    return e * bp
}

/**
 * Precession matrix (including frame bias) from GCRS to a specified
 * date, IAU 2006 model.
 */
fun eraPmat06(tt1: Double, tt2: Double): Matrix3D {
    // Bias-precession Fukushima-Williams angles.
    val (gamb, phib, psib, epsa) = eraPfw06(tt1, tt2)
    // Form the matrix.
    return eraFw2m(gamb, phib, psib, epsa)
}

/**
 * Precession angles, IAU 2006, equinox based.
 */
fun eraP06e(tt1: Double, tt2: Double): PrecessionAnglesIAU2006 {
    // Interval between fundamental date J2000.0 and given date (JC).
    val t = (tt1 - J2000 + tt2) / DAYSPERJC
    // Obliquity at J2000.0.
    val eps0 = 84381.406.arcsec
    // Luni-solar precession.
    val psia = ((5038.481507 + (-1.0790069 + (-0.00114045 + (0.000132851 - 0.0000000951 * t) * t) * t) * t) * t).arcsec
    // Inclination of mean equator with respect to the J2000.0 ecliptic.
    val oma = eps0 + ((-0.025754 + (0.0512623 + (-0.00772503 + (-0.000000467 + 0.0000003337 * t) * t) * t) * t) * t).arcsec
    // Ecliptic pole x, J2000.0 ecliptic triad.
    val bpa = ((4.199094 + (0.1939873 + (-0.00022466 + (-0.000000912 + 0.0000000120 * t) * t) * t) * t) * t).arcsec
    // Ecliptic pole -y, J2000.0 ecliptic triad.
    val bqa = ((-46.811015 + (0.0510283 + (0.00052413 + (-0.000000646 - 0.0000000172 * t) * t) * t) * t) * t).arcsec
    // Angle between moving and J2000.0 ecliptics.
    val pia = ((46.998973 + (-0.0334926 + (-0.00012559 + (0.000000113 - 0.0000000022 * t) * t) * t) * t) * t).arcsec
    // Longitude of ascending node of the moving ecliptic.
    val bpia = (629546.7936 + (-867.95758 + (0.157992 + (-0.0005371 + (-0.00004797 + 0.000000072 * t) * t) * t) * t) * t).arcsec
    // Mean obliquity of the ecliptic.
    val epsa = eraObl06(tt1, tt2)
    // Planetary precession.
    val chia = ((10.556403 + (-2.3814292 + (-0.00121197 + (0.000170663 - 0.0000000560 * t) * t) * t) * t) * t).arcsec
    // Equatorial precession: minus the third of the 323 Euler angles.
    val za = (-2.650545 + (2306.077181 + (1.0927348 + (0.01826837 + (-0.000028596 - 0.0000002904 * t) * t) * t) * t) * t).arcsec
    // Equatorial precession: minus the first of the 323 Euler angles.
    val zetaa = (2.650545 + (2306.083227 + (0.2988499 + (0.01801828 + (-0.000005971 - 0.0000003173 * t) * t) * t) * t) * t).arcsec
    // Equatorial precession: second of the 323 Euler angles.
    val thetaa = ((2004.191903 + (-0.4294934 + (-0.04182264 + (-0.000007089 - 0.0000001274 * t) * t) * t) * t) * t).arcsec
    // General precession.
    val pa = ((5028.796195 + (1.1054348 + (0.00007964 + (-0.000023857 - 0.0000000383 * t) * t) * t) * t) * t).arcsec
    // Fukushima-Williams angles for precession.
    val gam = ((10.556403 + (0.4932044 + (-0.00031238 + (-0.000002788 + 0.0000000260 * t) * t) * t) * t) * t).arcsec
    val phi = eps0 + ((-46.811015 + (0.0511269 + (0.00053289 + (-0.000000440 - 0.0000000176 * t) * t) * t) * t) * t).arcsec
    val psi = ((5038.481507 + (1.5584176 + (-0.00018522 + (-0.000026452 - 0.0000000148 * t) * t) * t) * t) * t).arcsec

    return PrecessionAnglesIAU2006(eps0, psia, oma, bpa, bqa, pia, bpia, epsa, chia, za, zetaa, thetaa, pa, gam, phi, psi)
}

/**
 * Form the matrix of nutation.
 *
 * @param epsa Mean obliquity of date.
 * @param dpsi Nutation angle.
 * @param deps Nutation angle.
 */
inline fun eraNumat(epsa: Angle, dpsi: Angle, deps: Angle): Matrix3D {
    return Matrix3D.rotX(epsa).rotateZ(-dpsi).rotateX(-(epsa + deps))
}

/**
 * Form the matrix of nutation for a given date, IAU 2006/2000A model.
 */
fun eraNum06a(tt1: Double, tt2: Double): Matrix3D {
    // Mean obliquity.
    val eps = eraObl06(tt1, tt2)
    // Nutation components.
    val (dp, de) = eraNut06a(tt1, tt2)
    // Nutation matrix.
    return eraNumat(eps, dp, de)
}

/**
 * Assemble the celestial to terrestrial matrix from equinox-based
 * components (the celestial-to-true matrix, the Greenwich Apparent
 * Sidereal Time and the polar motion matrix).
 *
 * @param rbpn Celestial-to-true matrix
 * @param gst  Greenwich (apparent) Sidereal Time (radians)
 * @param rpom Polar-motion matrix
 */
inline fun eraC2teqx(rbpn: Matrix3D, gst: Angle, rpom: Matrix3D): Matrix3D {
    return rpom * rbpn.rotateZ(gst)
}

/**
 * Form the celestial to terrestrial matrix given the date, the UT1,
 * the nutation and the polar motion. IAU 2000.
 *
 * The matrix rc2t transforms from celestial to terrestrial coordinates:
 *
 *    [TRS] = RPOM * R_3(GST) * RBPN * [CRS] = rc2t * [CRS]
 */
fun eraC2tpe(
    tt1: Double, tt2: Double, ut11: Double, ut12: Double,
    dpsi: Angle, deps: Angle, xp: Angle, yp: Angle
): Matrix3D {
    // Form the celestial-to-true matrix for this TT.
    val (epsa, _, _, _, _, rbpn) = eraPn00(tt1, tt2, dpsi, deps)
    // Predict the Greenwich Mean Sidereal Time for this UT1 and TT.
    val gmst = eraGmst00(ut11, ut12, tt1, tt2)
    // Predict the equation of the equinoxes given TT and nutation.
    val ee = eraEe00(tt1, tt2, epsa, dpsi)
    // Estimate s'.
    val sp = eraSp00(tt1, tt2)
    // Form the polar motion matrix.
    val rpom = eraPom00(xp, yp, sp)
    // Combine to form the celestial-to-terrestrial matrix.
    return eraC2teqx(rbpn, gmst + ee, rpom)
}

// The frame bias corrections in longitude and obliquity.
const val DPBIAS: Angle = -0.041775 * ASEC2RAD
const val DEBIAS: Angle = -0.0068192 * ASEC2RAD

/**
 * The ICRS RA of the J2000.0 equinox (Chapront et al., 2002).
 */
const val DRA0: Angle = -0.0146 * ASEC2RAD

/**
 *  Frame bias components of IAU 2000 precession-nutation models;  part
 *  of the Mathews-Herring-Buffett (MHB2000) nutation series, with
 *  additions.
 *
 *  @return longitude and obliquity corrections, and the ICRS RA of the J2000.0 mean equinox.
 */
inline fun eraBi00(): DoubleArray {
    return doubleArrayOf(DPBIAS, DEBIAS, DRA0)
}

/**
 * J2000.0 obliquity (Lieske et al. 1977)
 */
const val EPS0 = 84381.448 * ASEC2RAD

/**
 * Frame bias and precession, IAU 2000.
 *
 * @return frame bias matrix, precession matrix and bias-precession matrix.
 */
fun eraBp00(tt1: Double, tt2: Double): Array<Matrix3D> {
    val t = (tt1 - J2000 + tt2) / DAYSPERJC

    // Frame bias.
    val (dpsibi, depsbi, dra0) = eraBi00()

    // Precession angles (Lieske et al. 1977)
    val psia77 = (5038.7784 + (-1.07259 + (-0.001147) * t) * t) * t * ASEC2RAD
    val oma77 = EPS0 + ((0.05127 + (-0.007726) * t) * t) * t * ASEC2RAD
    val chia = (10.5526 + (-2.38064 + (-0.001125) * t) * t) * t * ASEC2RAD

    // Apply IAU 2000 precession corrections.
    val (dpsipr, depspr) = eraPr00(tt1, tt2)
    val psia = psia77 + dpsipr
    val oma = oma77 + depspr

    // Frame bias matrix: GCRS to J2000.0.
    val rb = Matrix3D.rotZ(dra0).rotateY(dpsibi * sin(EPS0)).rotateX(-depsbi)

    // Precession matrix: J2000.0 to mean of date.
    val rp = Matrix3D.rotX(EPS0).rotateZ(-psia).rotateX(-oma).rotateZ(chia)

    return arrayOf(rb, rp, rp * rb)
}

/**
 * Precession-nutation, IAU 2000 model:  a multi-purpose function,
 * supporting classical (equinox-based) use directly and CIO-based
 * use indirectly.
 */
fun eraPn00(tt1: Double, tt2: Double, dpsi: Angle, deps: Angle): PrecessionNutationMatrices {
    // IAU 2000 precession-rate adjustments.
    val (_, depspr) = eraPr00(tt1, tt2)

    // Mean obliquity, consistent with IAU 2000 precession-nutation.
    val epsa = eraObl80(tt1, tt2) + depspr

    // Frame bias and precession matrices and their product.
    val (rb, rp, rbp) = eraBp00(tt1, tt2)

    // Nutation matrix.
    val rn = eraNumat(epsa, dpsi, deps)

    // Bias-precession-nutation matrix (classical).
    val rbpn = rn * rbp

    return PrecessionNutationMatrices(epsa, rb, rp, rbp, rn, rbpn)
}

typealias StarDirectionCosines = DoubleArray
typealias TangentPointDirectionCosines = DoubleArray

/**
 * In the tangent plane projection, given the rectangular coordinates
 * of a star and its spherical coordinates, determine the spherical
 * coordinates of the tangent point.
 */
fun eraTpors(xi: Angle, eta: Angle, a: Angle, b: Angle): PairOfAngle? {
    val xi2 = xi * xi
    val r = sqrt(1.0 + xi2 + eta * eta)
    val sb = b.sin
    val cb = b.cos
    val rsb = r * sb
    val rcb = r * cb
    val w2 = rcb * rcb - xi2

    if (w2 >= 0.0) {
        var w = sqrt(w2)
        var s = rsb - eta * w
        var c = rsb * eta + w
        if (xi == 0.0 && w == 0.0) w = 1.0
        val a01 = (a - atan2(xi, w)).normalized
        val b01 = atan2(s, c).rad

        if (abs(rsb) < 1.0) return PairOfAngle(a01, b01)

        w = -w
        s = rsb - eta * w
        c = rsb * eta + w
        val a02 = (a - atan2(xi, w)).normalized
        val b02 = atan2(s, c).rad

        return PairOfAngle(a02, b02)
    } else {
        return null
    }
}

/**
 * In the tangent plane projection, given the rectangular coordinates
 * of a star and its direction cosines, determine the direction
 * cosines of the tangent point.
 */
fun eraTporv(xi: Angle, eta: Angle, v: StarDirectionCosines): TangentPointDirectionCosines? {
    val x = v[0]
    val y = v[1]
    val z = v[2]
    val rxy2 = x * x + y * y
    val xi2 = xi * xi
    val eta2p1 = eta * eta + 1.0
    val r = sqrt(xi2 + eta2p1)
    val rsb = r * z
    val rcb = r * hypot(x, y)
    val w2 = rcb * rcb - xi2

    if (w2 > 0.0) {
        var w = sqrt(w2)
        var c = (rsb * eta + w) / (eta2p1 * sqrt(rxy2 * (w2 + xi2)))

        val v00 = c * (x * w + y * xi)
        val v01 = c * (y * w - x * xi)
        val v02 = (rsb - eta * w) / eta2p1

        if (abs(rsb) < 1.0) return doubleArrayOf(v00, v01, v02)

        w = -w
        c = (rsb * eta + w) / (eta2p1 * sqrt(rxy2 * (w2 + xi2)))
        val v10 = c * (x * w + y * xi)
        val v11 = c * (y * w - x * xi)
        val v12 = (rsb - eta * w) / eta2p1

        return doubleArrayOf(v10, v11, v12)
    } else {
        return null
    }
}

/**
 * In the tangent plane projection, given the star's rectangular
 * coordinates and the spherical coordinates of the tangent point,
 * solve for the spherical coordinates of the star.
 */
fun eraTpsts(xi: Angle, eta: Angle, a0: Angle, b0: Angle): PairOfAngle {
    val sb0 = b0.sin
    val cb0 = b0.cos
    val d = cb0 - eta * sb0
    val a = (a0 + atan2(xi, d)).normalized
    val b = atan2(sb0 + eta * cb0, hypot(xi, d)).rad
    return PairOfAngle(a, b)
}

/**
 * In the tangent plane projection, given the star's rectangular
 * coordinates and the direction cosines of the tangent point, solve
 * for the direction cosines of the star.
 */
fun eraTpstv(xi: Angle, eta: Angle, v: TangentPointDirectionCosines): StarDirectionCosines {
    var x = v[0]
    val y = v[1]
    val z = v[2]

    var r = hypot(x, y)

    if (r == 0.0) {
        r = 1e-20
        x = r
    }

    val f = sqrt(1.0 + xi * xi + eta * eta)

    val v0 = (x - (xi * y + eta * x * z) / r) / f
    val v1 = (y + (xi * x - eta * y * z) / r) / f
    val v2 = (z + eta * r) / f

    return doubleArrayOf(v0, v1, v2)
}

/**
 * In the tangent plane projection, given celestial spherical
 * coordinates for a star and the tangent point, solve for the star's
 * rectangular coordinates in the tangent plane.
 */
fun eraTpxes(a: Angle, b: Angle, a0: Angle, b0: Angle): TangentPlaneCoordinate {
    val sb0 = b0.sin
    val sb = b.sin
    val cb0 = b0.cos
    val cb = b.cos
    val da = a - a0
    val sda = da.sin
    val cda = da.cos

    var d = sb * sb0 + cb * cb0 * cda

    val j = if (d > 1e-6) 0 // OK
    else if (d >= 0.0) 1 // star too far from axis
    else if (d > -1e-6) 2 // antistar on tangent plane
    else 3 // antistar too far from axis

    if (j == 1) d = 1e-6
    else if (j == 2) d = -1e-6

    val xi = cb * sda / d
    val eta = (sb * cb0 - cb * sb0 * cda) / d

    return TangentPlaneCoordinate(xi.rad, eta.rad, j)
}

/**
 * In the tangent plane projection, given celestial direction cosines
 * for a star and the tangent point, solve for the star's rectangular
 * coordinates in the tangent plane.
 */
fun eraTpxev(v: StarDirectionCosines, v0: TangentPointDirectionCosines): TangentPlaneCoordinate {
    val x = v[0]
    val y = v[1]
    val z = v[2]
    var x0 = v0[0]
    val y0 = v0[1]
    val z0 = v0[2]

    val r2 = x0 * x0 + y0 * y0
    var r = sqrt(r2)

    if (r == 0.0) {
        r = 1e-20
        x0 = r
    }

    val w = x * x0 + y * y0
    var d = w + z * z0

    val j = if (d > 1e-6) 0 // OK
    else if (d >= 0.0) 1 // star too far from axis
    else if (d > -1e-6) 2 // antistar on tangent plane
    else 3 // antistar too far from axis

    if (j == 1) d = 1e-6
    else if (j == 2) d = -1e-6

    d *= r
    val xi = (y * x0 - x * y0) / d
    val eta = (z * r2 - z0 * w) / d

    return TangentPlaneCoordinate(xi.rad, eta.rad, j)
}

/**
 * This function forms three Euler angles which implement general
 * precession from epoch J2000.0, using the IAU 2006 model.  Frame
 * bias (the offset between ICRS and mean J2000.0) is included.
 */
fun eraPb06(tt1: Double, tt2: Double): EulerAngles {
    // Precession matrix via Fukushima-Williams angles.
    var r = eraPmat06(tt1, tt2)

    // Solve for z, choosing the +/- pi alternative.
    var y = r[5]
    var x = -r[2]

    if (x < 0.0) {
        y = -y
        x = -x
    }

    val z = if (x != 0.0 || y != 0.0) (-atan2(y, x)).rad else 0.0

    // Derotate it out of the matrix.
    r = r.rotateZ(z)

    // Solve for the remaining two angles.
    y = r[2]
    x = r[8]

    val theta = if (x != 0.0 || y != 0.0) (-atan2(y, x)).rad else 0.0

    y = -r[3]
    x = r[4]

    val zeta = if (x != 0.0 || y != 0.0) (-atan2(y, x)).rad else 0.0

    return EulerAngles(zeta, z, theta)
}

inline fun roundToNearestWholeNumber(a: Double): Double {
    return if (abs(a) < 0.5) 0.0 else if (a < 0.0) ceil(a - 0.5) else floor(a + 0.5)
}

fun eraJd2Cal(dj1: Double, dj2: Double): Calendar {
    // Separate day and fraction (where -0.5 <= fraction < 0.5).
    var d = roundToNearestWholeNumber(dj1)
    val f1 = dj1 - d
    var jd = d.toLong()
    d = roundToNearestWholeNumber(dj2)
    val f2 = dj2 - d
    jd += d.toLong()


    // Compute f1+f2+0.5 using compensated summation (Klein 2006).
    var s = 0.5
    var cs = 0.0
    val v = doubleArrayOf(f1, f2)

    for (x in v) {
        val t = s + x
        cs += if (abs(s) >= abs(x)) (s - t) + x else (x - t) + s
        s = t

        if (s >= 1.0) {
            jd++
            s -= 1.0
        }
    }

    var f = s + cs
    cs = f - s

    // Deal with negative f.
    if (f < 0.0) {
        // Compensated summation: assume that |s| <= 1.0.
        f = s + 1.0
        cs += (1.0 - f) + s
        s = f
        f = s + cs
        cs = f - s
        jd--
    }

    // Deal with f that is 1.0 or more (when rounded to double).
    if ((f - 1.0) >= -DBL_EPSILON / 4.0) {
        // Compensated summation: assume that |s| <= 1.0.
        val t = s - 1.0
        cs += (s - t) - 1.0
        s = t
        f = s + cs

        if (-DBL_EPSILON / 2.0 < f) {
            jd++
            f = max(f, 0.0)
        }
    }

    // Express day in Gregorian calendar.
    var l = jd + 68569L
    val n = (4L * l) / 146097L
    l -= (146097L * n + 3L) / 4L
    val i = (4000L * (l + 1L)) / 1461001L
    l -= (1461L * i) / 4L - 31L
    val k = (80L * l) / 2447L
    val id = (l - (2447L * k) / 80L).toInt()
    l = k / 11L
    val im = (k + 2L - 12L * l).toInt()
    val iy = (100L * (n - 49L) + i + l).toInt()

    return Calendar(iy, im, id, f)
}

/**
 * Computes Gregorian Calendar to Modified Julian Date for 0hrs.
 */
fun eraCal2Jd(iy: Int, im: Int, id: Int): Double {
    val my = (im - 14) / 12L
    val iypmy = iy + my
    return ((1461L * (iypmy + 4800L)) / 4L + (367L * (im - 2 - 12 * my)) / 12L - (3L * ((iypmy + 4900L) / 100L)) / 4L + id - 2432076L).toDouble()
}

private data class LeapSecond(@JvmField val year: Int, @JvmField val month: Int, @JvmField val delat: Double)

private val LEAP_SECOND_CHANGES = arrayOf(
    LeapSecond(1960, 1, 1.4178180),
    LeapSecond(1961, 1, 1.4228180),
    LeapSecond(1961, 8, 1.3728180),
    LeapSecond(1962, 1, 1.8458580),
    LeapSecond(1963, 11, 1.9458580),
    LeapSecond(1964, 1, 3.2401300),
    LeapSecond(1964, 4, 3.3401300),
    LeapSecond(1964, 9, 3.4401300),
    LeapSecond(1965, 1, 3.5401300),
    LeapSecond(1965, 3, 3.6401300),
    LeapSecond(1965, 7, 3.7401300),
    LeapSecond(1965, 9, 3.8401300),
    LeapSecond(1966, 1, 4.3131700),
    LeapSecond(1968, 2, 4.2131700),
    LeapSecond(1972, 1, 10.0),
    LeapSecond(1972, 7, 11.0),
    LeapSecond(1973, 1, 12.0),
    LeapSecond(1974, 1, 13.0),
    LeapSecond(1975, 1, 14.0),
    LeapSecond(1976, 1, 15.0),
    LeapSecond(1977, 1, 16.0),
    LeapSecond(1978, 1, 17.0),
    LeapSecond(1979, 1, 18.0),
    LeapSecond(1980, 1, 19.0),
    LeapSecond(1981, 7, 20.0),
    LeapSecond(1982, 7, 21.0),
    LeapSecond(1983, 7, 22.0),
    LeapSecond(1985, 7, 23.0),
    LeapSecond(1988, 1, 24.0),
    LeapSecond(1990, 1, 25.0),
    LeapSecond(1991, 1, 26.0),
    LeapSecond(1992, 7, 27.0),
    LeapSecond(1993, 7, 28.0),
    LeapSecond(1994, 7, 29.0),
    LeapSecond(1996, 1, 30.0),
    LeapSecond(1997, 7, 31.0),
    LeapSecond(1999, 1, 32.0),
    LeapSecond(2006, 1, 33.0),
    LeapSecond(2009, 1, 34.0),
    LeapSecond(2012, 7, 35.0),
    LeapSecond(2015, 7, 36.0),
    LeapSecond(2017, 1, 37.0),
)

private val LEAP_SECOND_DRIFT = arrayOf(
    doubleArrayOf(37300.0, 0.0012960),
    doubleArrayOf(37300.0, 0.0012960),
    doubleArrayOf(37300.0, 0.0012960),
    doubleArrayOf(37665.0, 0.0011232),
    doubleArrayOf(37665.0, 0.0011232),
    doubleArrayOf(38761.0, 0.0012960),
    doubleArrayOf(38761.0, 0.0012960),
    doubleArrayOf(38761.0, 0.0012960),
    doubleArrayOf(38761.0, 0.0012960),
    doubleArrayOf(38761.0, 0.0012960),
    doubleArrayOf(38761.0, 0.0012960),
    doubleArrayOf(38761.0, 0.0012960),
    doubleArrayOf(39126.0, 0.0025920),
    doubleArrayOf(39126.0, 0.0025920),
)

/**
 *  For a given UTC date, calculate Delta(AT) = TAI-UTC.
 */
fun eraDat(iy: Int, im: Int, id: Int, fd: Double): Double {
    val djm = eraCal2Jd(iy, im, id)

    // Combine year and month to form a date-ordered integer...
    val m = 12 * iy + im

    // ...and use it to find the preceding table entry.
    val i = LEAP_SECOND_CHANGES.indexOfLast { m >= 12 * it.year + it.month }

    // Prevent underflow warnings.
    if (i < 0) return Double.NaN

    // Get the Delta(AT).
    var da = LEAP_SECOND_CHANGES[i].delat

    // If pre-1972, adjust for drift.
    if (LEAP_SECOND_CHANGES[i].year < 1972) {
        da += (djm + fd - LEAP_SECOND_DRIFT[i][0]) * LEAP_SECOND_DRIFT[i][1]
    }

    return da
}

inline fun eraDat(cal: Calendar, fraction: Double = cal.fraction): Double {
    return eraDat(cal.year, cal.month, cal.day, fraction)
}

/**
 * Universal Time, UT1, to Coordinated Universal Time, UTC.
 */
fun eraUt1Utc(ut11: Double, ut12: Double, dut1: Double): DoubleArray {
    val u1 = max(ut11, ut12)
    var u2 = min(ut11, ut12)

    var duts = dut1

    // See if the UT1 can possibly be in a leap-second day.
    var d1 = u1
    var dats1 = 0.0

    for (i in -1..3) {
        var d2 = u2 + i
        val cal = eraJd2Cal(d1, d2)
        val dats2 = eraDat(cal, 0.0)
        if (i == -1) dats1 = dats2
        val ddats = dats2 - dats1

        if (abs(ddats) >= 0.5) {
            // Yes, leap second nearby: ensure UT1-UTC is "before" value.
            if (ddats * duts >= 0.0) duts -= ddats

            // UT1 for the start of the UTC day that ends in a leap.
            d1 = MJD0
            d2 = eraCal2Jd(cal.year, cal.month, cal.day)

            val us1 = d1
            val us2 = d2 - 1.0 + duts / DAYSEC

            // Is the UT1 after this point?
            val du = u1 - us1 + (u2 - us2)

            if (du > 0.0) {
                // Yes: fraction of the current UTC day that has elapsed.
                val fd = du * DAYSEC / (DAYSEC + ddats)

                // Ramp UT1-UTC to bring about ERFA's JD(UTC) convention.
                duts += ddats * if (fd <= 1.0) fd else 1.0
            }

            break
        }

        dats1 = dats2
    }

    // Subtract the (possibly adjusted) UT1-UTC from UT1 to give UTC.
    u2 -= duts / DAYSEC

    return doubleArrayOf(u1, u2)
}

/**
 * Coordinated Universal Time, UTC, to International Atomic Time, TAI.
 */
fun eraUtcTai(utc1: Double, utc2: Double): DoubleArray {
    val u1 = max(utc1, utc2)
    val u2 = min(utc1, utc2)

    // Get TAI-UTC at 0h today.
    val cal = eraJd2Cal(u1, u2)
    val dat0 = eraDat(cal, 0.0)

    // Get TAI-UTC at 12h today (to detect drift).
    val dat12 = eraDat(cal, 0.5)

    // Get TAI-UTC at 0h tomorrow (to detect jumps).
    val calt = eraJd2Cal(u1 + 1.5, u2 - cal.fraction)
    val dat24 = eraDat(calt, 0.0)

    // Separate TAI-UTC change into per-day (DLOD) and any jump (DLEAP).
    val dlod = 2.0 * (dat12 - dat0)
    val dleap = dat24 - (dat0 + dlod)

    // Remove any scaling applied to spread leap into preceding day.
    var fd = cal.fraction * (DAYSEC + dleap) / DAYSEC

    // Scale from (pre-1972) UTC seconds to SI seconds.
    fd *= (DAYSEC + dlod) / DAYSEC

    // Today's calendar date to 2-part JD.
    val z = eraCal2Jd(cal.year, cal.month, cal.day)

    // Assemble the TAI result, preserving the UTC split and order.
    val a2 = (MJD0 - u1) + z + (fd + dat0 / DAYSEC)

    return doubleArrayOf(u1, a2)
}

/**
 * International Atomic Time, TAI, to Universal Time, UT1.
 */
inline fun eraTaiUt1(tai1: Double, tai2: Double, ut1MinusTai: Double): DoubleArray {
    return doubleArrayOf(tai1, tai2 + ut1MinusTai / DAYSEC)
}

/**
 * Coordinated Universal Time, UTC, to Universal Time, UT1.
 */
fun eraUtcUt1(utc1: Double, utc2: Double, dut1: Double): DoubleArray {
    val cal = eraJd2Cal(utc1, utc2)
    val dat = eraDat(cal)

    // Form UT1-TAI
    val dta = dut1 - dat

    val (tai1, tai2) = eraUtcTai(utc1, utc2)
    return eraTaiUt1(tai1, tai2, dta)
}

/**
 * International Atomic Time, TAI, to Coordinated Universal Time, UTC.
 */
fun eraTaiUtc(tai1: Double, tai2: Double): DoubleArray {
    var u2 = tai2

    // Iterate(though in most cases just once is enough).
    repeat(3) {
        // Guessed UTC to TAI.
        val (g1, g2) = eraUtcTai(tai1, u2)

        // Adjust guessed UTC.
        u2 += tai1 - g1
        u2 += tai2 - g2
    }

    return doubleArrayOf(tai1, u2)
}

/**
 * International Atomic Time, TAI, to Terrestrial Time, TT.
 */
inline fun eraTaiTt(tai1: Double, tai2: Double): DoubleArray {
    return doubleArrayOf(tai1, tai2 + TTMINUSTAI / DAYSEC)
}

/**
 * Terrestrial Time, TT, to International Atomic Time, TAI.
 */
inline fun eraTtTai(tt1: Double, tt2: Double): DoubleArray {
    return doubleArrayOf(tt1, tt2 - TTMINUSTAI / DAYSEC)
}

/**
 * Terrestrial Time, TT, to Barycentric Dynamical Time, TDB.
 */
inline fun eraTtTdb(tt1: Double, tt2: Double, tdbMinusTt: Double): DoubleArray {
    return doubleArrayOf(tt1, tt2 + tdbMinusTt / DAYSEC)
}

/**
 * Barycentric Dynamical Time, TDB, to Terrestrial Time, TT.
 */
inline fun eraTdbTt(tdb1: Double, tdb2: Double, tdbMinusTt: Double): DoubleArray {
    return doubleArrayOf(tdb1, tdb2 - tdbMinusTt / DAYSEC)
}

/**
 * Universal Time, UT1, to International Atomic Time, TAI.
 */
inline fun eraUt1Tai(ut11: Double, ut12: Double, ut1MinusTai: Double): DoubleArray {
    return doubleArrayOf(ut11, ut12 - ut1MinusTai / DAYSEC)
}

/**
 * Terrestrial Time, TT, to Universal Time, UT1.
 */
inline fun eraTtUt1(tt1: Double, tt2: Double, ttMinusUt1: Double): DoubleArray {
    return doubleArrayOf(tt1, tt2 - ttMinusUt1 / DAYSEC)
}

const val DBL_EPSILON = 2.220446049250313E-16
