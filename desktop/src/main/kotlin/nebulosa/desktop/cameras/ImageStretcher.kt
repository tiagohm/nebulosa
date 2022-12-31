package nebulosa.desktop.cameras

import javafx.fxml.FXML
import javafx.scene.control.Slider
import nebulosa.desktop.internal.Window

class ImageStretcher(private val imageViewer: ImageViewer) : Window("ImageStretcher") {

    @FXML private lateinit var shadow: Slider
    @FXML private lateinit var highlight: Slider
    @FXML private lateinit var midtone: Slider
    @FXML private lateinit var histogram: HistogramView

    init {
        isResizable = false

        titleProperty().bind(imageViewer.titleProperty())
    }

    override fun onStart() {
        shadow.valueProperty().addListener { _, _, value -> imageViewer.transformImage(shadow = value.toFloat() / 255f) }
        highlight.valueProperty().addListener { _, _, value -> imageViewer.transformImage(highlight = value.toFloat() / 255f) }
        midtone.valueProperty().addListener { _, _, value -> imageViewer.transformImage(midtone = value.toFloat() / 255f) }

        drawHistogram()
    }

    fun drawHistogram() {
        if (isShowing) {
            histogram.draw(imageViewer.fits!!)
        }
    }
}
