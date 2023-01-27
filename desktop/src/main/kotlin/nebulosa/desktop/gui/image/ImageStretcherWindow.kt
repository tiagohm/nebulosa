package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.Histogram
import nebulosa.desktop.logic.image.ImageStretcherManager
import org.controlsfx.control.RangeSlider

class ImageStretcherWindow(private val window: ImageWindow) : AbstractWindow() {

    override val resourceName = "ImageStretcher"

    override val icon = "nebulosa-image-stretcher"

    @FXML private lateinit var shadowSpinner: Spinner<Double>
    @FXML private lateinit var midtoneSpinner: Spinner<Double>
    @FXML private lateinit var highlightSpinner: Spinner<Double>
    @FXML private lateinit var shadowAndHighlightRangeSlider: RangeSlider
    @FXML private lateinit var midtoneSlider: Slider
    @FXML private lateinit var histogram: Histogram

    private val imageStretcherManager = ImageStretcherManager(this)

    init {
        isResizable = false
        title = "Image Stretch"
    }

    override fun onCreate() {
        shadowAndHighlightRangeSlider.lowValueProperty().on(::onShadowChanged)
        shadowAndHighlightRangeSlider.highValueProperty().on(::onHighlightChanged)
        midtoneSlider.valueProperty().on(::onMidtoneChanged)

        shadowSpinner.valueProperty().on { shadowAndHighlightRangeSlider.lowValue = it!!.toDouble() }
        highlightSpinner.valueProperty().on { shadowAndHighlightRangeSlider.highValue = it!!.toDouble() }
        midtoneSpinner.valueProperty().on { midtoneSlider.value = it!!.toDouble() }
    }

    override fun onStart() {
        highlight = window.highlight * 255f
        shadow = window.shadow * 255f
        midtone = window.midtone * 255f

        updateTitle()
    }

    var shadow
        get() = shadowSpinner.value.toFloat()
        set(value) {
            shadowSpinner.valueFactory.value = value.toDouble()
        }

    var highlight
        get() = highlightSpinner.value.toFloat()
        set(value) {
            highlightSpinner.valueFactory.value = value.toDouble()
        }

    var midtone
        get() = midtoneSpinner.value.toFloat()
        set(value) {
            midtoneSpinner.valueFactory.value = value.toDouble()
        }

    private fun onShadowChanged(value: Double) {
        shadowSpinner.valueFactory.value = value
        imageStretcherManager.apply()
    }

    private fun onHighlightChanged(value: Double) {
        highlightSpinner.valueFactory.value = value
        imageStretcherManager.apply()
    }

    private fun onMidtoneChanged(value: Double) {
        midtoneSpinner.valueFactory.value = value
        imageStretcherManager.apply()
    }

    fun apply(shadow: Float, highlight: Float, midtone: Float) {
        window.applySTF(shadow, highlight, midtone)
    }

    fun drawHistogram() {
        histogram.draw(window.fits ?: return)
    }

    fun updateTitle() {
        title = "Image Stretch · " + window.title.split("·").last().trim()
    }
}
