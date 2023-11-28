package nebulosa.watney.plate.solving

import nebulosa.fits.Header
import nebulosa.math.Angle
import nebulosa.plate.solving.Parity

internal data class ComputedPlateSolution(
    @JvmField val header: Header = Header(),
    @JvmField val orientation: Angle = 0.0,
    @JvmField val pixelScale: Double = 0.0, // arcsec/px
    @JvmField val centerRA: Angle = 0.0,
    @JvmField val centerDEC: Angle = 0.0,
    @JvmField val width: Angle = 0.0,
    @JvmField val height: Angle = 0.0,
    @JvmField val radius: Angle = 0.0,
    @JvmField val parity: Parity = Parity.NORMAL,
    @JvmField val plateConstants: PlateConstants = PlateConstants.EMPTY,
)
