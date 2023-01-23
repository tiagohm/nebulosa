package nebulosa.desktop.logic.image

import nebulosa.desktop.gui.image.SCNRWindow

class SCNRManager(private val window: SCNRWindow) {

    fun apply() {
        window.applySCNR(window.enabled, window.channel, window.protectionMethod, window.amount)
    }
}
