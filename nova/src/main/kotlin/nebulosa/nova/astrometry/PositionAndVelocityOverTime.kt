package nebulosa.nova.astrometry

import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime

fun interface PositionAndVelocityOverTime {

    fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D>
}
