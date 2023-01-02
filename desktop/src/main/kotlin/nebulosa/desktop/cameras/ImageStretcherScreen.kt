package nebulosa.desktop.cameras

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.Slider
import nebulosa.desktop.core.controls.Screen

class ImageStretcherScreen(private val imageViewer: ImageViewerScreen) :
    Screen("ImageStretcher", "nebulosa-image-stretcher"), ChangeListener<Number> {

    @FXML private lateinit var shadow: Slider
    @FXML private lateinit var highlight: Slider
    @FXML private lateinit var midtone: Slider
    @FXML private lateinit var histogram: HistogramView

    init {
        isResizable = false

        titleProperty().bind(imageViewer.titleProperty())
    }

    override fun onStart() {
        shadow.value = imageViewer.shadow * 255.0
        highlight.value = imageViewer.highlight * 255.0
        midtone.value = imageViewer.midtone * 255.0

        shadow.valueProperty().addListener(this)
        highlight.valueProperty().addListener(this)
        midtone.valueProperty().addListener(this)

        drawHistogram()
    }

    override fun onStop() {
        shadow.valueProperty().removeListener(this)
        highlight.valueProperty().removeListener(this)
        midtone.valueProperty().removeListener(this)
    }

    override fun changed(
        observable: ObservableValue<out Number>,
        oldValue: Number, newValue: Number,
    ) {
        if (observable === shadow.valueProperty()) imageViewer.transformImage(shadow = newValue.toFloat() / 255f)
        else if (observable === highlight.valueProperty()) imageViewer.transformImage(highlight = newValue.toFloat() / 255f)
        else if (observable === midtone.valueProperty()) imageViewer.transformImage(midtone = newValue.toFloat() / 255f)
    }

    fun drawHistogram() {
        if (isShowing) {
            histogram.draw(imageViewer.fits!!)
        }
    }
}
