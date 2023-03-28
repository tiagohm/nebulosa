package nebulosa.desktop.view.guider

import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.view.View
import nebulosa.guiding.GuidePoint
import nebulosa.guiding.StarPoint
import nebulosa.imaging.Image

interface GuiderView : View, ImageViewer.MouseListener {

    fun updateStatus(text: String)

    fun updateStarProfile(
        image: Image, regionSize: Double,
        lockPosition: GuidePoint, primaryStar: StarPoint,
    )
}
