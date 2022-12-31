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
        imageViewer.shadow.bind(shadow.valueProperty().divide(255.0))
        imageViewer.highlight.bind(highlight.valueProperty().divide(255.0))
        imageViewer.midtone.bind(midtone.valueProperty().divide(255.0))

        draw()
    }

    fun draw() {
        if (isShowing) {
            histogram.draw(imageViewer.fits!!)
        }
    }
}
