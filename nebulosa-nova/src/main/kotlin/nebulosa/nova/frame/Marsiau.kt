package nebulosa.nova.frame

/**
 * Mars Mean Equator and IAU vector of J2000.
 *
 * The IAU-vector at Mars is the point
 * on the mean equator of Mars where the equator
 * ascends through the earth mean equator.
 * This vector is the cross product of Earth
 * mean north with Mars mean north.
 */
@Suppress("FloatingPointLiteralPrecision")
object Marsiau : InertialFrame(
    0.67325774746002498, 0.73940787491414595, -3.6947768825436786E-17,
    -0.58963083782625325, 0.53688031082163401, 0.60340285625473833,
    0.44616082366044196, -0.40624564781301037, 0.79743651350036859,
)
