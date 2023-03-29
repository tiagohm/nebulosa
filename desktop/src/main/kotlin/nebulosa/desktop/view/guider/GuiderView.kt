package nebulosa.desktop.view.guider

import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.view.View
import nebulosa.guiding.Guider
import nebulosa.imaging.Image

interface GuiderView : View, ImageViewer.MouseListener {

    fun updateStatus(text: String)

    fun updateStarProfile(guider: Guider, image: Image = guider.image!!)
}
