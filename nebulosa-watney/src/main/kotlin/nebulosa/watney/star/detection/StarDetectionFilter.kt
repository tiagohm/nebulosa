package nebulosa.watney.star.detection

import nebulosa.imaging.Image

/**
 * Interface for filtering stars after initial star detection.
 */
interface StarDetectionFilter {

    fun filter(bins: MutableList<StarPixelBin>, image: Image)
}
