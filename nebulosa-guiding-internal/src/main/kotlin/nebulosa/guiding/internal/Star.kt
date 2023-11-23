package nebulosa.guiding.internal

import nebulosa.hfd.FindMode
import nebulosa.hfd.FindResult
import nebulosa.hfd.HalfFluxDiameter
import nebulosa.imaging.Image

/**
 * Represents a star.
 */
open class Star : Point, StarPoint {

    final override var mass = 0.0
        private set

    final override var snr = 0.0
        private set

    final override var hfd = 0.0
        private set

    final override var peak = 0.0
        private set

    var findResult = FindResult.ERROR
        private set

    constructor(x: Double = 0.0, y: Double = 0.0) : super(x, y, false)

    constructor(point: Point) : super(point)

    internal fun find(
        image: Image, searchRegion: Double = 15.0,
        baseX: Double = x, baseY: Double = y,
        mode: FindMode = FindMode.CENTROID, minHFD: Double = 1.5,
    ): Boolean {
        val star = HalfFluxDiameter.compute(image, baseX, baseY, searchRegion, mode, minHFD)
        mass = star.mass
        snr = star.snr
        this.hfd = star.hfd
        peak = star.peak
        set(star.x, star.y)
        findResult = star.result
        return wasFound
    }

    override fun invalidate() {
        mass = 0.0
        snr = 0.0
        hfd = 0.0
        findResult = FindResult.ERROR
        super.invalidate()
    }

    override val wasFound
        get() = valid && (findResult == FindResult.OK || findResult == FindResult.SATURATED)

    override fun toString(): String {
        return "Star(valid=$valid, x=$x, y=$y," +
                " mass=$mass, snr=$snr, hfd=$hfd, peak=$peak," +
                " findResult=$findResult)"
    }
}
