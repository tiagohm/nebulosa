package nebulosa.nova.frame

import nebulosa.time.InstantOfTime

/**
 * The dynamical frame of the Earth’s true equator and equinox of date.
 *
 * This is supplied as an explicit reference frame in case you want
 * |xyz| coordinates; if you want angles, it’s better to use the
 * standard position method ``radec(epoch='date')`` since that will
 * return the conventional units of hours-of-right-ascension instead of
 * the degrees-of-longitude that ``frame_latlon()`` would return.
 *
 * This reference frame combines current theories of the Earth’s
 * precession and nutation with a small offset between the ITRS and
 * J2000 systems to produce right ascension and declination for a given
 * date relative to the Earth’s axis and equator of rotation.
 */
data object TrueEquatorAndEquinoxOfDate : Frame {

    override fun rotationAt(time: InstantOfTime) = time.m
}
