package nebulosa.nova.frame

import nebulosa.erfa.*
import nebulosa.math.Matrix3D
import nebulosa.time.IERS
import nebulosa.time.InstantOfTime

fun cirsToItrs(time: InstantOfTime): Matrix3D {
    // Compute the polar motion p-matrix.
    val (xp, yp) = IERS.pmXY(time)
    val sp = eraSp00(time.tt.whole, time.tt.fraction)
    val pm = eraPom00(xp, yp, sp)
    // Now determine the Earth Rotation Angle.
    val era = eraEra00(time.ut1.whole, time.ut1.fraction)
    // c2tcio expects a GCRS->CIRS matrix, but we just set that to an I-matrix
    // because we're already in CIRS.
    return eraC2tcio(Matrix3D.IDENTITY, era, pm)
}

fun teteToItrs(
    time: InstantOfTime,
    rbpn: Matrix3D? = null,
): Matrix3D {
    val (xp, yp) = IERS.pmXY(time)
    val sp = eraSp00(time.tt.whole, time.tt.fraction)
    val pm = eraPom00(xp, yp, sp)
    // Now determine the greenwich apparent sidereal time.
    // We use the 2006A model for consistency with RBPN matrix use in GCRS <-> TETE.
    val gast = if (rbpn != null) {
        eraGst06(time.ut1.whole, time.ut1.fraction, time.tt.whole, time.tt.fraction, rbpn)
    } else {
        eraGst06a(time.ut1.whole, time.ut1.fraction, time.tt.whole, time.tt.fraction)
    }
    // c2tcio expects a GCRS->CIRS matrix, but we just set that to an I-matrix
    // because we're already in CIRS.
    return eraC2tcio(Matrix3D.IDENTITY, gast, pm)
}

fun trueEclipticRotation(time: InstantOfTime): Matrix3D {
    val rnpb = eraPnm06a(time.tt.whole, time.tt.fraction)
    return rnpb.rotateX(time.trueObliquity)
}
