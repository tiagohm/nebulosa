package nebulosa.desktop.gui.atlas

import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.view.atlas.DeepSkyFilterView
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.controlsfx.control.RangeSlider

class DeepSkyFilterWindow : AbstractWindow("AtlasDeepSkyFilter", "filter"), DeepSkyFilterView {

    @FXML private lateinit var regionEnabledCheckbox: CheckBox
    @FXML private lateinit var raTextField: TextField
    @FXML private lateinit var decTextField: TextField
    @FXML private lateinit var radiusSpinner: Spinner<Double>
    @FXML private lateinit var constellationChoiceBox: ChoiceBox<Constellation>
    @FXML private lateinit var typeChoiceBox: ChoiceBox<SkyObjectType>
    @FXML private lateinit var magnitudeRangeSlider: RangeSlider

    init {
        resizable = false
        title = "Atlas Filter"
    }

    override fun onCreate() {
        raTextField.disableProperty().bind(!regionEnabledCheckbox.selectedProperty())
        decTextField.disableProperty().bind(!regionEnabledCheckbox.selectedProperty())
        radiusSpinner.disableProperty().bind(!regionEnabledCheckbox.selectedProperty())

        constellationChoiceBox.converter = ConstellationStringConverter
        constellationChoiceBox.items.add(null)
        constellationChoiceBox.items.addAll(Constellation.values().toList())

        typeChoiceBox.converter = SkyObjectTypeStringConverter
        typeChoiceBox.items.add(null)
        typeChoiceBox.items.addAll(SkyObjectType.values().sortedBy { it.description })
    }

    override fun onStart() {
        filtered = false
    }

    override var filtered = false
        private set

    override val rightAscension
        get() = Angle.from(raTextField.text, true) ?: Angle.ZERO

    override val declination
        get() = Angle.from(decTextField.text) ?: Angle.ZERO

    override val radius
        get() = if (regionEnabledCheckbox.isSelected) radiusSpinner.value!!.deg else Angle.ZERO

    override val constellation: Constellation?
        get() = constellationChoiceBox.value

    override val type: SkyObjectType?
        get() = typeChoiceBox.value

    override val mangitudeMin
        get() = magnitudeRangeSlider.lowValue

    override val magnitudeMax
        get() = magnitudeRangeSlider.highValue

    @FXML
    private fun filter() {
        filtered = true
        close()
    }

    private object ConstellationStringConverter : StringConverter<Constellation>() {

        override fun toString(constellation: Constellation?) = if (constellation == null) "All"
        else "%s (%s)".format(constellation.latinName, constellation.iau)

        override fun fromString(text: String?) = null
    }

    private object SkyObjectTypeStringConverter : StringConverter<SkyObjectType>() {

        override fun toString(type: SkyObjectType?) = type?.description ?: "All"

        override fun fromString(text: String?) = null
    }
}
