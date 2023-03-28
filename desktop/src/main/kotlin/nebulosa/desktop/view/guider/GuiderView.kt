package nebulosa.desktop.view.guider

import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.view.View

interface GuiderView : View, ImageViewer.MouseListener {

    fun updateStatus(text: String)
}
