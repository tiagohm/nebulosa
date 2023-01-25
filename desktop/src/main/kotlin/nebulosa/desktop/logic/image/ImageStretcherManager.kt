package nebulosa.desktop.logic.image

import nebulosa.desktop.gui.image.ImageStretcherWindow

class ImageStretcherManager(private val window: ImageStretcherWindow) {

    fun apply() {
        window.apply(window.shadow / 255f, window.highlight / 255f, window.midtone / 255f)
        window.drawHistogram()
    }
}
