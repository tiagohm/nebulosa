package nebulosa.nova.position

import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.ICRF
import nebulosa.time.InstantOfTime

/**
 * An astrometric |xyz| position relative to a particular observer.
 *
 * The astrometric position of a body is its position relative to an
 * observer, adjusted for light-time delay.  It is the position of the
 * body back when it emitted (or reflected) the light that is now
 * reaching the observer's eye or telescope.
 */
class Astrometric internal constructor(
    position: Vector3D,
    velocity: Vector3D,
    time: InstantOfTime,
    center: Number,
    target: Number,
    val barycenter: Barycentric,
) : ICRF(position, velocity, time, center, target) {

    /**
     * Computes an [Apparent] position for this body.
     *
     * This applies two effects to the position that arise from
     * relativity and shift slightly where the other body will appear
     * in the sky: the deflection that the image will experience if its
     * light passes close to large masses in the Solar System, and the
     * aberration of light caused by the observer's own velocity.
     *
     * The deflection is computed from the Sun, Jupiter and Saturn provided by [ephemeris].
     */
//    fun apparent(ephemeris: Ephemeris): Apparent {
//        val bcrsp = barycenter.position
//        val bcrsv = barycenter.velocity
//        val gcrs = barycenter.gcrs
//
//        val includeEarthDeflection = gcrs != null && limb(position, gcrs).second >= 0.8
//
//        var p = deflection(position, bcrsp, ephemeris, time, includeEarthDeflection)
//        p = aberration(p, bcrsv, lightTime)
//
//        val a = Apparent(p, velocity, time, center, target, barycenter)
//        a.gcrs = gcrs
//        return a
//    }
}
