package nebulosa.guiding.internal

import nebulosa.imaging.Image
import nebulosa.imaging.hfd.FindMode
import nebulosa.imaging.hfd.HalfFluxDiameter
import kotlin.math.roundToInt

/**
 * Represents a star.
 */
open class Star : Point, StarPoint {

    final override var flux = 0f
        private set

    final override var snr = 0f
        private set

    final override var hfd = 0f
        private set

    constructor(x: Double = 0.0, y: Double = 0.0) : super(x, y, false)

    constructor(point: Point) : super(point)

    internal fun find(
        image: Image, searchRegion: Int = 15,
        baseX: Double = x, baseY: Double = y,
        mode: FindMode = FindMode.CENTROID,
    ): Boolean {
        val star = HalfFluxDiameter.compute(image, baseX.roundToInt(), baseY.roundToInt(), searchRegion, mode)
        flux = star.flux
        snr = star.snr
        hfd = star.hfd
        set(star.x.toDouble(), star.y.toDouble())
        return wasFound
    }

    override fun invalidate() {
        flux = 0f
        snr = 0f
        hfd = 0f
        super.invalidate()
    }

    override val wasFound
        get() = valid && (hfd > 0f)

    override fun toString() = "Star(valid=$valid, x=$x, y=$y," +
            " mass=$flux, snr=$snr, hfd=$hfd"
}
