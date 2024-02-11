package nebulosa.api.alignment.polar.tppa

import nebulosa.math.Angle

interface TPPAListener {

    fun slewStarted(step: TPPAStep, rightAscension: Angle, declination: Angle)

    fun solverStarted(step: TPPAStep)

    fun solverFinished(step: TPPAStep, rightAscension: Angle, declination: Angle)

    fun polarAlignmentComputed(step: TPPAStep, azimuth: Angle, altitude: Angle)

    fun solverFailed(step: TPPAStep)

    fun polarAlignmentFinished(step: TPPAStep, aborted: Boolean)
}
