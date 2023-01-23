package nebulosa.desktop.imageviewer

import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.util.StringConverter
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.onTwo
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.gui.view.HistogramView
import nebulosa.math.map
import org.controlsfx.control.RangeSlider

// TODO: Stretch by channels: R, G, B, K(both)
class ImageStretcherScreen(private val imageViewerScreen: ImageViewerScreen) :
    Screen("ImageStretcher", "nebulosa-image-stretcher") {

    @FXML private lateinit var bitDepth: ChoiceBox<Int>
    @FXML private lateinit var shadow: Spinner<Double>
    @FXML private lateinit var midtoneSpinner: Spinner<Double>
    @FXML private lateinit var highlight: Spinner<Double>
    @FXML private lateinit var shadowAndHighlight: RangeSlider
    @FXML private lateinit var midtone: Slider
    @FXML private lateinit var histogram: HistogramView

    init {
        isResizable = false
    }

    override fun onCreate() {
        bitDepth.converter = BitDepthStringConverter

        updateTitle()

        imageViewerScreen.titleProperty().on { updateTitle() }

        shadowAndHighlight.lowValueProperty().on(::onLowValueChanged)
        shadowAndHighlight.highValueProperty().on(::onHighValueChanged)
        midtone.valueProperty().on(::onMidtoneValueChanged)
        shadow.valueProperty().on { shadowAndHighlight.lowValue = it!!.toDouble() }
        highlight.valueProperty().on { shadowAndHighlight.highValue = it!!.toDouble() }
        midtoneSpinner.valueProperty().on { midtone.value = it!!.toDouble() }

        with(shadow.valueFactory as DoubleSpinnerValueFactory) {
            minProperty().bind(shadowAndHighlight.minProperty())
            maxProperty().bind(shadowAndHighlight.maxProperty())
        }

        with(highlight.valueFactory as DoubleSpinnerValueFactory) {
            minProperty().bind(shadowAndHighlight.minProperty())
            maxProperty().bind(shadowAndHighlight.maxProperty())
        }

        with(midtoneSpinner.valueFactory as DoubleSpinnerValueFactory) {
            minProperty().bind(midtone.minProperty())
            maxProperty().bind(midtone.maxProperty())
        }

        bitDepth.valueProperty().onTwo { prev, value ->
            val shadowValue = shadowAndHighlight.lowValue
            val highlightValue = shadowAndHighlight.highValue
            val midtoneValue = midtone.value

            if (prev != null) {
                if (value == 16) {
                    shadowAndHighlight.max = 65535.0
                    shadowAndHighlight.min = 0.0
                    shadowAndHighlight.lowValue = map(shadowValue, 0.0, 255.0, 0.0, 65535.0)
                    shadowAndHighlight.highValue = map(highlightValue, 0.0, 255.0, 0.0, 65535.0)
                    shadowAndHighlight.majorTickUnit = 16384.0
                    midtone.max = 65535.0
                    midtone.min = 0.0
                    midtone.value = map(midtoneValue, 0.0, 255.0, 0.0, 65535.0)
                    midtone.majorTickUnit = 16384.0
                } else {
                    shadowAndHighlight.max = 255.0
                    shadowAndHighlight.min = 0.0
                    shadowAndHighlight.lowValue = map(shadowValue, 0.0, 65535.0, 0.0, 255.0)
                    shadowAndHighlight.highValue = map(highlightValue, 0.0, 65535.0, 0.0, 255.0)
                    shadowAndHighlight.majorTickUnit = 32.0
                    midtone.max = 255.0
                    midtone.min = 0.0
                    midtone.value = map(midtoneValue, 0.0, 65535.0, 0.0, 255.0)
                    midtone.majorTickUnit = 32.0
                }
            }
        }
    }

    override fun onStart() {
        val bitDepthFactor = if (bitDepth.value == 16) 65535.0 else 255.0
        shadowAndHighlight.lowValue = imageViewerScreen.shadow * bitDepthFactor
        shadowAndHighlight.highValue = imageViewerScreen.highlight * bitDepthFactor
        midtone.value = imageViewerScreen.midtone * bitDepthFactor

        if (bitDepth.value == null) bitDepth.selectionModel.selectFirst()

        drawHistogram()
    }

    private fun onLowValueChanged(value: Double) {
        val bitDepthFactor = if (bitDepth.value == 16) 65535f else 255f
        shadow.valueFactory.value = value
        imageViewerScreen.transformImage(shadow = value.toFloat() / bitDepthFactor)
        drawHistogram()
    }

    private fun onHighValueChanged(value: Double) {
        val bitDepthFactor = if (bitDepth.value == 16) 65535f else 255f
        highlight.valueFactory.value = value
        imageViewerScreen.transformImage(highlight = value.toFloat() / bitDepthFactor)
        drawHistogram()
    }

    private fun onMidtoneValueChanged(value: Double) {
        val bitDepthFactor = if (bitDepth.value == 16) 65535f else 255f
        midtoneSpinner.valueFactory.value = value
        imageViewerScreen.transformImage(midtone = value.toFloat() / bitDepthFactor)
        drawHistogram()
    }

    fun drawHistogram() {
        if (isShowing) {
            val fits = imageViewerScreen.transformedFits ?: imageViewerScreen.fits ?: return
            histogram.draw(fits)
        }
    }

    private fun updateTitle() {
        val name = imageViewerScreen.title.split("·").last().trim()
        title = "Image Stretcher · $name"
    }

    private object BitDepthStringConverter : StringConverter<Int>() {

        override fun toString(bitDepth: Int?) = if (bitDepth == null) "-" else "$bitDepth bits"

        override fun fromString(text: String?) = null
    }
}
