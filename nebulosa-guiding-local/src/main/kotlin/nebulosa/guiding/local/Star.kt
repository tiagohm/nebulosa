package nebulosa.guiding.local

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.star.hfd.FindMode
import nebulosa.imaging.algorithms.star.hfd.FindResult
import nebulosa.imaging.algorithms.star.hfd.HalfFluxDiameter

/**
 * Represents a star.
 */
open class Star : Point {

    var mass = 0f
        private set

    var snr = 0f
        private set

    var hfd = 0f
        private set

    var peak = 0f
        private set

    var lastFindResult = FindResult.ERROR
        private set

    constructor(x: Float, y: Float) : super(x, y)

    constructor(point: Point) : super(point)

    fun find(
        image: Image, searchRegion: Float = 15f,
        baseX: Float = x, baseY: Float = y,
        mode: FindMode = FindMode.CENTROID, minHFD: Float = 1.5f,
    ): Boolean {
        val hfd = HalfFluxDiameter(baseX, baseY, searchRegion, mode, minHFD)
        val star = hfd.compute(image)
        mass = star.mass
        snr = star.snr
        this.hfd = star.hfd
        peak = star.peak
        lastFindResult = star.result
        return wasFound
    }

    override fun invalidate() {
        mass = 0f
        snr = 0f
        hfd = 0f
        lastFindResult = FindResult.ERROR
        super.invalidate()
    }

    inline val wasFound
        get() = isValid && (lastFindResult == FindResult.OK || lastFindResult == FindResult.SATURATED)

    override fun toString(): String {
        return "Star(x=$x, y=$y," +
                " mass=$mass, snr=$snr, hfd=$hfd, peak=$peak," +
                " lastFindResult=$lastFindResult)"
    }
}
