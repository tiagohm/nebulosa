package nebulosa.desktop.logic.image

import nebulosa.desktop.view.image.SCNRView

class SCNRManager(private val view: SCNRView) {

    suspend fun apply() {
        view.applySCNR(view.enabled, view.channel, view.protectionMethod, view.amount)
    }
}
