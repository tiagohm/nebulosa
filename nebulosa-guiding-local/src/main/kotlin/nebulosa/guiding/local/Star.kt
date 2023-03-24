package nebulosa.guiding.local

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.star.hfd.FindMode
import nebulosa.imaging.algorithms.star.hfd.FindResult
import nebulosa.imaging.algorithms.star.hfd.HalfFluxDiameter

/**
 * Represents a star.
 */
open class Star : Point {

    var mass = 0.0
        private set

    var snr = 0.0
        private set

    var hfd = 0.0
        private set

    var peak = 0.0
        private set

    var lastFindResult = FindResult.ERROR
        private set

    constructor(x: Double, y: Double) : super(x, y, false)

    constructor(point: Point) : super(point)

    fun find(
        image: Image, searchRegion: Double = 15.0,
        baseX: Double = x, baseY: Double = y,
        mode: FindMode = FindMode.CENTROID, minHFD: Double = 1.5,
    ): Boolean {
        val hfd = HalfFluxDiameter(baseX, baseY, searchRegion, mode, minHFD)
        val star = hfd.compute(image)
        mass = star.mass
        snr = star.snr
        this.hfd = star.hfd
        peak = star.peak
        set(star.x, star.y)
        lastFindResult = star.result
        return wasFound
    }

    override fun invalidate() {
        mass = 0.0
        snr = 0.0
        hfd = 0.0
        lastFindResult = FindResult.ERROR
        super.invalidate()
    }

    inline val wasFound
        get() = valid && (lastFindResult == FindResult.OK || lastFindResult == FindResult.SATURATED)

    override fun toString(): String {
        return "Star(x=$x, y=$y," +
                " mass=$mass, snr=$snr, hfd=$hfd, peak=$peak," +
                " lastFindResult=$lastFindResult)"
    }
}
