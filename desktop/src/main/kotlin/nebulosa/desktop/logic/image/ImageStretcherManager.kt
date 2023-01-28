package nebulosa.desktop.logic.image

import nebulosa.desktop.view.image.ImageStretcherView

class ImageStretcherManager(private val view: ImageStretcherView) {

    fun apply() {
        view.apply(view.shadow / 255f, view.highlight / 255f, view.midtone / 255f)
        view.drawHistogram()
    }
}
