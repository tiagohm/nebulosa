package nebulosa.imaging.algorithms

import nebulosa.imaging.Image

/**
 * Dark subtraction algorithm:
 *     Pedestal = max(median(dark_frame) - median(light_frame), 0) - handles overall gain/gradient differences
 *     Dark_corrected(i) = min(max(light(i) + pedestal - dark(i), 0), 65335)
 */
class DarkSubtraction(
    val darkFrame: Image,
) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        return source
    }
}
