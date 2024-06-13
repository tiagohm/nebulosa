package nebulosa.watney.stardetector

import nebulosa.stardetector.StarPoint

data class Star(
    override val x: Double = 0.0,
    override val y: Double = 0.0,
    val size: Double = 0.0,
    override var hfd: Double = 0.0,
    override var snr: Double = 0.0,
    override var flux: Double = 0.0,
) : StarPoint
