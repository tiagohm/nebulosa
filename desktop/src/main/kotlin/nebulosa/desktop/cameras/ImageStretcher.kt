package nebulosa.desktop.cameras

import javafx.fxml.FXML
import javafx.scene.control.Slider
import nebulosa.desktop.internal.Window

class ImageStretcher(private val imageViewer: ImageViewer) : Window("ImageStretcher") {

    @FXML private lateinit var shadow: Slider
    @FXML private lateinit var highlight: Slider
    @FXML private lateinit var midtone: Slider

    init {
        isResizable = false

        titleProperty().bind(imageViewer.titleProperty())

        shadow.valueProperty().addListener { _, _, value -> imageViewer.shadow = value.toInt() / 255f }
        highlight.valueProperty().addListener { _, _, value -> imageViewer.highlight = value.toInt() / 255f }
        midtone.valueProperty().addListener { _, _, value -> imageViewer.midtone = value.toInt() / 255f }
    }
}
