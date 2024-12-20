package nebulosa.watney.stardetector

import nebulosa.image.Image

/**
 * Interface for filtering stars after initial star detection.
 */
interface StarDetectionFilter {

    fun filter(bins: MutableList<StarPixelBin>, image: Image)
}
