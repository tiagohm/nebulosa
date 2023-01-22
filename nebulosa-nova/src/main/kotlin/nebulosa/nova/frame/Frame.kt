package nebulosa.nova.frame

import nebulosa.math.Matrix3D
import nebulosa.time.InstantOfTime

interface Frame {

    /**
     * Gets the rotation matrix at [time].
     */
    fun rotationAt(time: InstantOfTime): Matrix3D

    fun dRdtTimesRtAt(time: InstantOfTime): Matrix3D? = null
}
