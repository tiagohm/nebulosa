package nebulosa.nova.astrometry

import nebulosa.erfa.PositionAndVelocity
import nebulosa.time.InstantOfTime

fun interface PositionAndVelocityOverTime {

    fun compute(time: InstantOfTime): PositionAndVelocity
}
