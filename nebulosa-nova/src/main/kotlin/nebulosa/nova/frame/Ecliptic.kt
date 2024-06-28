package nebulosa.nova.frame

import nebulosa.math.Matrix3D
import nebulosa.time.InstantOfTime

data object Ecliptic : Frame {

    override fun rotationAt(time: InstantOfTime) = Matrix3D.rotX(-time.trueObliquity) * time.m
}
