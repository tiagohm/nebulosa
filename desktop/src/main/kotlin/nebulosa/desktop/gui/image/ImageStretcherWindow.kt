package nebulosa.desktop.gui.image

import javafx.animation.PauseTransition
import javafx.fxml.FXML
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.util.Duration
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.image.ImageStretcherManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.view.image.ImageStretcherView
import nebulosa.desktop.view.image.ImageView
import org.controlsfx.control.RangeSlider

class ImageStretcherWindow(private val view: ImageView) : AbstractWindow("ImageStretcher", "histogram"), ImageStretcherView {

    @FXML private lateinit var shadowSpinner: Spinner<Double>
    @FXML private lateinit var midtoneSpinner: Spinner<Double>
    @FXML private lateinit var highlightSpinner: Spinner<Double>
    @FXML private lateinit var shadowAndHighlightRangeSlider: RangeSlider
    @FXML private lateinit var midtoneSlider: Slider
    @FXML private lateinit var histogramView: HistogramView

    private val imageStretcherManager = ImageStretcherManager(this)
    private val stretchParameterListener = PauseTransition(Duration.seconds(0.5))

    init {
        title = "Image Stretch"
        resizable = false

        stretchParameterListener.setOnFinished {
            launch { imageStretcherManager.apply(shadow / 255f, highlight / 255f, midtone / 255f) }
        }
    }

    override fun onCreate() {
        shadowAndHighlightRangeSlider.lowValue = 0.0
        shadowAndHighlightRangeSlider.highValue = 255.0

        shadowAndHighlightRangeSlider.lowValueProperty().on {
            stretchParameterListener.playFromStart()
            shadowSpinner.valueFactory.value = it
        }
        shadowAndHighlightRangeSlider.highValueProperty().on {
            stretchParameterListener.playFromStart()
            highlightSpinner.valueFactory.value = it
        }
        midtoneSlider.valueProperty().on {
            stretchParameterListener.playFromStart()
            midtoneSpinner.valueFactory.value = it
        }

        shadowSpinner.valueProperty().on { shadowAndHighlightRangeSlider.lowValue = it!!.toDouble() }
        highlightSpinner.valueProperty().on { shadowAndHighlightRangeSlider.highValue = it!!.toDouble() }
        midtoneSpinner.valueProperty().on { midtoneSlider.value = it!!.toDouble() }
    }

    override fun onStart() {
        launch { updateTitle() }
        launch { updateStretchParameters(view.shadow, view.highlight, view.midtone) }
        launch { drawHistogram() }
    }

    override val shadow
        get() = shadowSpinner.value.toFloat()

    override val highlight
        get() = highlightSpinner.value.toFloat()

    override val midtone
        get() = midtoneSpinner.value.toFloat()

    override suspend fun apply(shadow: Float, highlight: Float, midtone: Float) {
        view.stf(shadow, highlight, midtone)
    }

    override suspend fun drawHistogram() {
        val image = view.image ?: return
        withIO { histogramView.compute(image) }
        withMain { histogramView.draw() }
    }

    override suspend fun updateTitle() = withMain {
        title = "Image Stretch · " + view.title.split("·").last().trim()
    }

    override suspend fun updateStretchParameters(shadow: Float, highlight: Float, midtone: Float) = withMain {
        shadowSpinner.valueFactory.value = shadow * 255.0
        highlightSpinner.valueFactory.value = highlight * 255.0
        midtoneSpinner.valueFactory.value = midtone * 255.0
    }

    @FXML
    override fun autoStretch() {
        launch { imageStretcherManager.autoStretch(view.originalImage ?: return@launch) }
    }

    override suspend fun resetStretch(onlyParameters: Boolean) {
        if (onlyParameters) updateStretchParameters(0f, 1f, 0.5f)
        else imageStretcherManager.resetStretch()
    }

    @FXML
    private fun resetStretch() {
        launch { resetStretch(false) }
    }
}
