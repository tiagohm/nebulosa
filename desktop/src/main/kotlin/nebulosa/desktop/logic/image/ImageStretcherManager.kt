package nebulosa.desktop.logic.image

import nebulosa.desktop.view.image.ImageStretcherView
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.AutoScreenTransformFunction

class ImageStretcherManager(private val view: ImageStretcherView) {

    fun autoStretch(image: Image) {
        val params = AutoScreenTransformFunction.compute(image)
        view.updateStretchParameters(params.shadow, params.highlight, params.midtone)
        apply(params.shadow, params.highlight, params.midtone)
    }

    fun resetStretch() {
        view.updateStretchParameters(0f, 1f, 0.5f)
        apply(0f, 1f, 0.5f)
    }

    fun apply(shadow: Float, highlight: Float, midtone: Float) {
        view.apply(shadow, highlight, midtone)
        view.drawHistogram()
    }
}
