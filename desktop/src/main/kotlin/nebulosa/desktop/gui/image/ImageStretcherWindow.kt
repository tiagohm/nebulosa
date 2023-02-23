package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.Histogram
import nebulosa.desktop.logic.image.ImageStretcherManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.view.image.ImageStretcherView
import nebulosa.desktop.view.image.ImageView
import org.controlsfx.control.RangeSlider

class ImageStretcherWindow(private val view: ImageView) : AbstractWindow("ImageStretcher", "nebulosa-image-stretcher"), ImageStretcherView {

    @FXML private lateinit var shadowSpinner: Spinner<Double>
    @FXML private lateinit var midtoneSpinner: Spinner<Double>
    @FXML private lateinit var highlightSpinner: Spinner<Double>
    @FXML private lateinit var shadowAndHighlightRangeSlider: RangeSlider
    @FXML private lateinit var midtoneSlider: Slider
    @FXML private lateinit var histogram: Histogram

    private val imageStretcherManager = ImageStretcherManager(this)

    init {
        resizable = false
        title = "Image Stretch"
    }

    override fun onCreate() {
        shadowAndHighlightRangeSlider.lowValue = 0.0
        shadowAndHighlightRangeSlider.highValue = 255.0

        shadowAndHighlightRangeSlider.lowValueProperty().on(::onShadowChanged)
        shadowAndHighlightRangeSlider.highValueProperty().on(::onHighlightChanged)
        midtoneSlider.valueProperty().on(::onMidtoneChanged)

        shadowSpinner.valueProperty().on { shadowAndHighlightRangeSlider.lowValue = it!!.toDouble() }
        highlightSpinner.valueProperty().on { shadowAndHighlightRangeSlider.highValue = it!!.toDouble() }
        midtoneSpinner.valueProperty().on { midtoneSlider.value = it!!.toDouble() }
    }

    override fun onStart() {
        highlight = view.highlight * 255f
        shadow = view.shadow * 255f
        midtone = view.midtone * 255f

        updateTitle()
        drawHistogram()
    }

    override var shadow
        get() = shadowSpinner.value.toFloat()
        set(value) {
            shadowSpinner.valueFactory.value = value.toDouble()
        }

    override var highlight
        get() = highlightSpinner.value.toFloat()
        set(value) {
            highlightSpinner.valueFactory.value = value.toDouble()
        }

    override var midtone
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

    override fun apply(shadow: Float, highlight: Float, midtone: Float) {
        view.applySTF(shadow, highlight, midtone)
    }

    override fun drawHistogram() {
        histogram.draw(view.fits ?: return)
    }

    override fun updateTitle() {
        title = "Image Stretch · " + view.title.split("·").last().trim()
    }
}
