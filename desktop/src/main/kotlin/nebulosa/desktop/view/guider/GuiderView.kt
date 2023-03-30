package nebulosa.desktop.view.guider

import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.view.View
import nebulosa.guiding.GuideStats
import nebulosa.guiding.Guider
import nebulosa.imaging.Image

interface GuiderView : View, ImageViewer.MouseListener {

    fun updateStatus(text: String)

    fun updateStarProfile(guider: Guider, image: Image = guider.image!!)

    fun updateGraph(stats: List<GuideStats>, maxRADuration: Double, maxDECDuration: Double)

    fun updateGraphInfo(rmsRA: Double, rmsDEC: Double, rmsTotal: Double, pixelScale: Double)
}
