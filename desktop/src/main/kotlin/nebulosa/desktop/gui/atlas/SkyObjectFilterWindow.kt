package nebulosa.desktop.gui.atlas

import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.ComboBoxItem
import nebulosa.desktop.gui.control.LabeledPane
import nebulosa.desktop.logic.on
import nebulosa.desktop.view.atlas.SkyObjectFilterView
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.controlsfx.control.RangeSlider

class SkyObjectFilterWindow : AbstractWindow("SkyObjectFilter", "filter"), SkyObjectFilterView {

    @FXML private lateinit var regionEnabledCheckbox: CheckBox
    @FXML private lateinit var raTextField: TextField
    @FXML private lateinit var decTextField: TextField
    @FXML private lateinit var radiusSpinner: Spinner<Double>
    @FXML private lateinit var constellationComboBox: ComboBox<ComboBoxItem<Constellation>>
    @FXML private lateinit var typeComboBox: ComboBox<ComboBoxItem<SkyObjectType>>
    @FXML private lateinit var magnitudeRangeSlider: RangeSlider

    init {
        title = "Sky Object Filter"
        resizable = false
    }

    override fun onCreate() {
        raTextField.disableProperty().bind(!regionEnabledCheckbox.selectedProperty())
        decTextField.disableProperty().bind(!regionEnabledCheckbox.selectedProperty())
        radiusSpinner.disableProperty().bind(!regionEnabledCheckbox.selectedProperty())

        constellationComboBox.converter = ConstellationStringConverter
        constellationComboBox.items.add(0, ComboBoxItem.Null())
        constellationComboBox.items.addAll(Constellation.values().map { ComboBoxItem.Valued(it) })

        typeComboBox.converter = SkyObjectTypeStringConverter
        typeComboBox.items.add(0, ComboBoxItem.Null())
        typeComboBox.items.addAll(STAR_OBJECT_TYPES.map { ComboBoxItem.Valued(it) })

        val magnitudeRangeLabel = magnitudeRangeSlider.parent as LabeledPane
        magnitudeRangeSlider.lowValueProperty().on { magnitudeRangeLabel.text = MAGNITUDE_LABEL_TEXT.format(it, magnitudeRangeSlider.highValue) }
        magnitudeRangeSlider.highValueProperty().on { magnitudeRangeLabel.text = MAGNITUDE_LABEL_TEXT.format(magnitudeRangeSlider.lowValue, it) }
    }

    override fun onStart() {
        filtered = false

        if (constellationComboBox.value == null) {
            constellationComboBox.selectionModel.selectFirst()
        }

        if (typeComboBox.value == null) {
            typeComboBox.selectionModel.selectFirst()
        }
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
        get() = constellationComboBox.value?.item

    override val type: SkyObjectType?
        get() = typeComboBox.value?.item

    override val mangitudeMin
        get() = magnitudeRangeSlider.lowValue

    override val magnitudeMax
        get() = magnitudeRangeSlider.highValue

    @FXML
    private fun filter() {
        filtered = true
        close()
    }

    private object ConstellationStringConverter : StringConverter<ComboBoxItem<Constellation>>() {

        override fun toString(constellation: ComboBoxItem<Constellation>?) = constellation?.item
            ?.let { "%s (%s)".format(constellation.item!!.latinName, constellation.item!!.iau) }
            ?: "All"

        override fun fromString(text: String?) = null
    }

    private object SkyObjectTypeStringConverter : StringConverter<ComboBoxItem<SkyObjectType>>() {

        override fun toString(type: ComboBoxItem<SkyObjectType>?) = type?.item?.description ?: "All"

        override fun fromString(text: String?) = null
    }

    companion object {

        private const val MAGNITUDE_LABEL_TEXT = "Magnitude (min: %.1f max: %.1f)"

        @JvmStatic private val STAR_OBJECT_TYPES = listOf(
            SkyObjectType.ACTIVE_GALAXY_NUCLEUS,
            SkyObjectType.ALPHA2_CVN_VARIABLE,
            SkyObjectType.ASSOCIATION_OF_STARS,
            SkyObjectType.ASYMPTOTIC_GIANT_BRANCH_STAR,
            SkyObjectType.BETA_CEP_VARIABLE,
            SkyObjectType.BE_STAR,
            SkyObjectType.BLAZAR,
            SkyObjectType.BLUE_COMPACT_GALAXY,
            SkyObjectType.BLUE_STRAGGLER,
            SkyObjectType.BLUE_SUPERGIANT,
            SkyObjectType.BL_LAC,
            SkyObjectType.BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG,
            SkyObjectType.BY_DRA_VARIABLE,
            SkyObjectType.CARBON_STAR,
            SkyObjectType.CATACLYSMIC_BINARY,
            SkyObjectType.CEPHEID_VARIABLE,
            SkyObjectType.CHEMICALLY_PECULIAR_STAR,
            SkyObjectType.CLASSICAL_CEPHEID_VARIABLE,
            SkyObjectType.CLASSICAL_NOVA,
            SkyObjectType.CLUSTER_OF_GALAXIES,
            SkyObjectType.CLUSTER_OF_STARS,
            SkyObjectType.COMPACT_GROUP_OF_GALAXIES,
            SkyObjectType.COMPOSITE_OBJECT_BLEND,
            SkyObjectType.DARK_CLOUD_NEBULA,
            SkyObjectType.DELTA_SCT_VARIABLE,
            SkyObjectType.DOUBLE_OR_MULTIPLE_STAR,
            SkyObjectType.ECLIPSING_BINARY,
            SkyObjectType.ELLIPSOIDAL_VARIABLE,
            SkyObjectType.EMISSION_LINE_GALAXY,
            SkyObjectType.EMISSION_LINE_STAR,
            SkyObjectType.EMISSION_OBJECT,
            SkyObjectType.ERUPTIVE_VARIABLE,
            SkyObjectType.EVOLVED_SUPERGIANT,
            SkyObjectType.GALAXY,
            SkyObjectType.GALAXY_IN_PAIR_OF_GALAXIES,
            SkyObjectType.GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES,
            SkyObjectType.GALAXY_TOWARDS_A_GROUP_OF_GALAXIES,
            SkyObjectType.GAMMA_DOR_VARIABLE,
            SkyObjectType.GLOBULAR_CLUSTER,
            SkyObjectType.GROUP_OF_GALAXIES,
            SkyObjectType.HERBIG_AE_BE_STAR,
            SkyObjectType.HERBIG_HARO_OBJECT,
            SkyObjectType.HIGH_MASS_X_RAY_BINARY,
            SkyObjectType.HIGH_PROPER_MOTION_STAR,
            SkyObjectType.HIGH_VELOCITY_STAR,
            SkyObjectType.HII_GALAXY,
            SkyObjectType.HII_REGION,
            SkyObjectType.HI_21CM_SOURCE,
            SkyObjectType.HORIZONTAL_BRANCH_STAR,
            SkyObjectType.HOT_SUBDWARF,
            SkyObjectType.INFRA_RED_SOURCE,
            SkyObjectType.INTERACTING_GALAXIES,
            SkyObjectType.INTERSTELLAR_MEDIUM_OBJECT,
            SkyObjectType.INTERSTELLAR_SHELL,
            SkyObjectType.IRREGULAR_VARIABLE,
            SkyObjectType.LINER_TYPE_ACTIVE_GALAXY_NUCLEUS,
            SkyObjectType.LONG_PERIOD_VARIABLE,
            SkyObjectType.LOW_MASS_STAR,
            SkyObjectType.LOW_MASS_X_RAY_BINARY,
            SkyObjectType.LOW_SURFACE_BRIGHTNESS_GALAXY,
            SkyObjectType.MAIN_SEQUENCE_STAR,
            SkyObjectType.MIRA_VARIABLE,
            SkyObjectType.MOLECULAR_CLOUD,
            SkyObjectType.NEBULA,
            SkyObjectType.NOT_AN_OBJECT_ERROR_ARTEFACT,
            SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
            SkyObjectType.OH_IR_STAR,
            SkyObjectType.OPEN_CLUSTER,
            SkyObjectType.ORION_VARIABLE,
            SkyObjectType.PAIR_OF_GALAXIES,
            SkyObjectType.PART_OF_A_GALAXY,
            SkyObjectType.PLANETARY_NEBULA,
            SkyObjectType.POST_AGB_STAR,
            SkyObjectType.PULSATING_VARIABLE,
            SkyObjectType.QUASAR,
            SkyObjectType.RADIO_GALAXY,
            SkyObjectType.RADIO_SOURCE,
            SkyObjectType.RED_GIANT_BRANCH_STAR,
            SkyObjectType.RED_SUPERGIANT,
            SkyObjectType.REFLECTION_NEBULA,
            SkyObjectType.REGION_DEFINED_IN_THE_SKY,
            SkyObjectType.ROTATING_VARIABLE,
            SkyObjectType.RR_LYRAE_VARIABLE,
            SkyObjectType.RS_CVN_VARIABLE,
            SkyObjectType.RV_TAURI_VARIABLE,
            SkyObjectType.R_CRB_VARIABLE,
            SkyObjectType.SEYFERT_1_GALAXY,
            SkyObjectType.SEYFERT_2_GALAXY,
            SkyObjectType.SEYFERT_GALAXY,
            SkyObjectType.SPECTROSCOPIC_BINARY,
            SkyObjectType.STAR,
            SkyObjectType.STARBURST_GALAXY,
            SkyObjectType.SUPERNOVA,
            SkyObjectType.SUPERNOVA_REMNANT,
            SkyObjectType.SX_PHE_VARIABLE,
            SkyObjectType.SYMBIOTIC_STAR,
            SkyObjectType.S_STAR,
            SkyObjectType.TYPE_II_CEPHEID_VARIABLE,
            SkyObjectType.T_TAURI_STAR,
            SkyObjectType.VARIABLE_STAR,
            SkyObjectType.WHITE_DWARF,
            SkyObjectType.WOLF_RAYET,
            SkyObjectType.X_RAY_BINARY,
            SkyObjectType.YELLOW_SUPERGIANT,
            SkyObjectType.YOUNG_STELLAR_OBJECT,
        )
    }
}
