package nebulosa.desktop.imageviewer

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.util.StringConverter
import nebulosa.desktop.core.scene.Screen
import nebulosa.math.map
import org.controlsfx.control.RangeSlider

class ImageStretcherScreen(private val imageViewer: ImageViewerScreen) :
    Screen("ImageStretcher", "nebulosa-image-stretcher"), ChangeListener<Number> {

    @FXML private lateinit var bitDepth: ChoiceBox<Int>
    @FXML private lateinit var shadow: Spinner<Double>
    @FXML private lateinit var midtoneSpinner: Spinner<Double>
    @FXML private lateinit var highlight: Spinner<Double>
    @FXML private lateinit var shadowAndHighlight: RangeSlider
    @FXML private lateinit var midtone: Slider
    @FXML private lateinit var histogram: HistogramView

    init {
        title = "Image Stretcher"
        isResizable = false
    }

    override fun onCreate() {
        bitDepth.converter = BitDepthStringConverter

        shadowAndHighlight.lowValueProperty().addListener(this)
        shadowAndHighlight.highValueProperty().addListener(this)
        midtone.valueProperty().addListener(this)
        shadow.valueProperty().addListener(this)
        highlight.valueProperty().addListener(this)
        midtoneSpinner.valueProperty().addListener(this)

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

        bitDepth.valueProperty().addListener { _, prev, value ->
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
        shadowAndHighlight.lowValue = imageViewer.shadow * bitDepthFactor
        shadowAndHighlight.highValue = imageViewer.highlight * bitDepthFactor
        midtone.value = imageViewer.midtone * bitDepthFactor

        if (bitDepth.value == null) bitDepth.selectionModel.selectFirst()

        drawHistogram()
    }

    override fun changed(
        observable: ObservableValue<out Number>,
        oldValue: Number, newValue: Number,
    ) {
        val bitDepthFactor = if (bitDepth.value == 16) 65535f else 255f

        if (observable === shadowAndHighlight.lowValueProperty()) {
            shadow.valueFactory.value = newValue.toDouble()
            imageViewer.transformImage(shadow = newValue.toFloat() / bitDepthFactor)
        } else if (observable === shadowAndHighlight.highValueProperty()) {
            highlight.valueFactory.value = newValue.toDouble()
            imageViewer.transformImage(highlight = newValue.toFloat() / bitDepthFactor)
        } else if (observable === midtone.valueProperty()) {
            midtoneSpinner.valueFactory.value = newValue.toDouble()
            imageViewer.transformImage(midtone = newValue.toFloat() / bitDepthFactor)
        } else if (observable === shadow.valueProperty()) {
            shadowAndHighlight.lowValue = newValue.toDouble()
        } else if (observable === highlight.valueProperty()) {
            shadowAndHighlight.highValue = newValue.toDouble()
        } else if (observable === midtoneSpinner.valueProperty()) {
            midtone.value = newValue.toDouble()
        }
    }

    fun drawHistogram() {
        if (isShowing) {
            histogram.draw(imageViewer.fits!!)
        }
    }

    private object BitDepthStringConverter : StringConverter<Int>() {

        override fun toString(o: Int?) = if (o == null) "" else "$o bits"

        override fun fromString(string: String?) = string?.replace(" bits", "")?.toInt()
    }
}
