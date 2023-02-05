package nebulosa.nova.frame

import nebulosa.math.Matrix3D
import nebulosa.time.InstantOfTime

object Ecliptic : Frame {

    override fun rotationAt(time: InstantOfTime) = Matrix3D.rotateX(-time.trueObliquity) * time.m
}
