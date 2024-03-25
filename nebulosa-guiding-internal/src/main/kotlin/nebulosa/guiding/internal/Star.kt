package nebulosa.guiding.internal

import nebulosa.image.Image
import nebulosa.image.algorithms.computation.hfd.HFD
import kotlin.math.roundToInt

/**
 * Represents a star.
 */
open class Star : Point, StarPoint {

    final override var flux = 0.0
        private set

    final override var snr = 0.0
        private set

    final override var hfd = 0.0
        private set

    constructor(x: Double = 0.0, y: Double = 0.0) : super(x, y, false)

    constructor(point: Point) : super(point)

    internal fun find(
        image: Image, searchRegion: Int = 15,
        baseX: Double = x, baseY: Double = y,
        mode: HFD.Mode = HFD.Mode.CENTROID,
    ): Boolean {
        val star = HFD.compute(image, baseX.roundToInt(), baseY.roundToInt(), searchRegion, mode)
        flux = star.flux
        snr = star.snr
        hfd = star.hfd
        set(star.x.toDouble(), star.y.toDouble())
        return wasFound
    }

    override fun invalidate() {
        flux = 0.0
        snr = 0.0
        hfd = 0.0
        super.invalidate()
    }

    override val wasFound
        get() = valid && (hfd > 0.0)

    override fun toString() = "Star(valid=$valid, x=$x, y=$y," +
            " mass=$flux, snr=$snr, hfd=$hfd"
}
