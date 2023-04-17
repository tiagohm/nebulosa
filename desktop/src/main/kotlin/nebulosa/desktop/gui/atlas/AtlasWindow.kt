package nebulosa.desktop.gui.atlas

import eu.hansolo.fx.charts.data.XYItem
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.util.Callback
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.CopyableLabel
import nebulosa.desktop.gui.control.LabeledPane
import nebulosa.desktop.gui.control.PropertyValueFactory
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.atlas.AtlasManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.math.Angle
import nebulosa.math.AngleFormatter
import nebulosa.math.PairOfAngle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyCatalogFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.util.function.Predicate

@Component
class AtlasWindow : AbstractWindow("Atlas", "sky"), AtlasView {

    @Lazy @Autowired private lateinit var atlasManager: AtlasManager

    @FXML private lateinit var ephemerisTabPane: TabPane
    @FXML private lateinit var nameLabel: Label
    @FXML private lateinit var rightAscensionLabel: CopyableLabel
    @FXML private lateinit var declinationLabel: CopyableLabel
    @FXML private lateinit var rightAscensionJ2000Label: CopyableLabel
    @FXML private lateinit var declinationJ2000Label: CopyableLabel
    @FXML private lateinit var altitudeLabel: CopyableLabel
    @FXML private lateinit var azimuthLabel: CopyableLabel
    @FXML private lateinit var extra1Label: CopyableLabel
    @FXML private lateinit var extra2Label: CopyableLabel
    @FXML private lateinit var extra3Label: CopyableLabel
    @FXML private lateinit var extra4Label: CopyableLabel
    @FXML private lateinit var constellationLabel: CopyableLabel
    @FXML private lateinit var rtsLabel: CopyableLabel
    @FXML private lateinit var sunView: SunView
    @FXML private lateinit var moonView: MoonView
    @FXML private lateinit var planetTableView: TableView<AtlasView.Planet>
    @FXML private lateinit var searchMinorPlanetTextField: TextField
    @FXML private lateinit var minorPlanetTableView: TableView<AtlasView.MinorPlanet>
    @FXML private lateinit var searchStarTextField: TextField
    @FXML private lateinit var starTableView: TableView<AtlasView.Star>
    @FXML private lateinit var searchDSOTextField: TextField
    @FXML private lateinit var dsosTableView: TableView<AtlasView.DSO>
    @FXML private lateinit var goToButton: Button
    @FXML private lateinit var slewToButton: Button
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var frameButton: Button
    @FXML private lateinit var altitudeChart: AltitudeChart

    @Volatile private var started = false

    private lateinit var extraLabels: Array<CopyableLabel>
    private lateinit var extraPanes: Array<LabeledPane>

    init {
        resizable = false
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate() {
        extraLabels = arrayOf(extra1Label, extra2Label, extra3Label, extra4Label)
        extraPanes = Array(3) { extraLabels[it].parent as LabeledPane }

        val isNotConnected = !atlasManager.mountProperty.connectedProperty
        val isMoving = atlasManager.mountProperty.slewingProperty or atlasManager.mountProperty.parkingProperty
        val isComputing = atlasManager.computing

        atlasManager.initialize()

        ephemerisTabPane.disableProperty().bind(isComputing)

        goToButton.disableProperty().bind(atlasManager.mountProperty.isNull or isNotConnected or isMoving or isComputing)

        slewToButton.disableProperty().bind(goToButton.disableProperty())

        syncButton.disableProperty().bind(goToButton.disableProperty())

        frameButton.disableProperty().bind(isComputing)

        (planetTableView.columns[0] as TableColumn<AtlasView.Planet, String>).cellValueFactory = PropertyValueFactory { it.name }
        (planetTableView.columns[1] as TableColumn<AtlasView.Planet, String>).cellValueFactory = PropertyValueFactory { it.type }
        planetTableView.selectionModel.selectedItemProperty()
            .on { if (it != null) launch { atlasManager.computePlanet(it) } }

        (minorPlanetTableView.columns[0] as TableColumn<AtlasView.MinorPlanet, String>).cellValueFactory = PropertyValueFactory { it.element }
        (minorPlanetTableView.columns[1] as TableColumn<AtlasView.MinorPlanet, String>).cellValueFactory = PropertyValueFactory { it.description }
        (minorPlanetTableView.columns[2] as TableColumn<AtlasView.MinorPlanet, String>).cellValueFactory = PropertyValueFactory { it.value }

        (starTableView.columns[0] as TableColumn<AtlasView.Star, String>).cellValueFactory = PropertyValueFactory { it.name }
        (starTableView.columns[1] as TableColumn<AtlasView.Star, Double>).cellValueFactory = PropertyValueFactory { it.magnitude }
        (starTableView.columns[1] as TableColumn<AtlasView.Star, Double>).cellFactory = Callback { _ -> MagnitudeTableCell<AtlasView.Star>() }
        (starTableView.columns[2] as TableColumn<AtlasView.Star, String>).cellValueFactory = PropertyValueFactory { it.constellation }
        starTableView.selectionModel.selectedItemProperty()
            .on { if (it != null) launch { atlasManager.computeStar(it) } }

        (dsosTableView.columns[0] as TableColumn<AtlasView.DSO, String>).cellValueFactory = PropertyValueFactory { it.name }
        (dsosTableView.columns[1] as TableColumn<AtlasView.DSO, Double>).cellValueFactory = PropertyValueFactory { it.magnitude }
        (dsosTableView.columns[1] as TableColumn<AtlasView.DSO, Double>).cellFactory = Callback { _ -> MagnitudeTableCell<AtlasView.DSO>() }
        (dsosTableView.columns[2] as TableColumn<AtlasView.DSO, String>).cellValueFactory = PropertyValueFactory { it.type }
        (dsosTableView.columns[3] as TableColumn<AtlasView.DSO, String>).cellValueFactory = PropertyValueFactory { it.constellation }
        dsosTableView.selectionModel.selectedItemProperty()
            .on { if (it != null) launch { atlasManager.computeDSO(it) } }

        launch { atlasManager.populatePlanets() }
        launch { atlasManager.populateStars() }
        launch { atlasManager.populateDSOs() }
    }

    override fun onStart() {
        started = true

        launch { atlasManager.loadPreferences() }
        launch { atlasManager.updateSunImage() }
        launch { atlasManager.updateMoonImage() }
        launch { atlasManager.computeTab(AtlasView.TabType.SUN) }
    }

    override fun onStop() {
        atlasManager.savePreferences()
    }

    @FXML
    private fun tabSelectionChanged(event: Event) {
        if (!started) return
        if (!(event.source as Tab).isSelected) return
        val userData = ephemerisTabPane.selectionModel.selectedItem.userData as String
        val tabType = AtlasView.TabType.valueOf(userData)
        launch { atlasManager.computeTab(tabType) }
    }

    val equatorialCoordinate
        get() = PairOfAngle(Angle.from(rightAscensionLabel.text, true)!!, Angle.from(declinationLabel.text)!!)

    val equatorialJ2000Coordinate
        get() = PairOfAngle(Angle.from(rightAscensionJ2000Label.text, true)!!, Angle.from(declinationJ2000Label.text)!!)

    @FXML
    private fun goTo() {
        val (ra, dec) = equatorialCoordinate
        atlasManager.goTo(ra, dec)
    }

    @FXML
    private fun slewTo() {
        val (ra, dec) = equatorialCoordinate
        atlasManager.slewTo(ra, dec)
    }

    @FXML
    private fun sync() {
        val (ra, dec) = equatorialCoordinate
        atlasManager.sync(ra, dec)
    }

    @FXML
    private fun frame() {
        val (ra, dec) = equatorialJ2000Coordinate
        launch { atlasManager.frame(ra, dec) }
    }

    @FXML
    private fun searchMinorPlanet() {
        val text = searchMinorPlanetTextField.text.trim().ifEmpty { null } ?: return
        launch { atlasManager.searchMinorPlanet(text) }
    }

    @FXML
    @Suppress("UNCHECKED_CAST")
    private fun searchStar() {
        val text = searchStarTextField.text.trim()
        with((starTableView.items as SortedList<*>).source as FilteredList<AtlasView.Star>) {
            predicate = StarFilter(text)
        }
    }

    @FXML
    @Suppress("UNCHECKED_CAST")
    private fun searchDSO() {
        val text = searchDSOTextField.text.trim()
        with((dsosTableView.items as SortedList<*>).source as FilteredList<AtlasView.DSO>) {
            predicate = DSOFilter(text)
        }
    }

    override suspend fun drawAltitude(
        points: List<XYItem>,
        now: Double,
        civilDawn: DoubleArray, nauticalDawn: DoubleArray, astronomicalDawn: DoubleArray,
        civilDusk: DoubleArray, nauticalDusk: DoubleArray, astronomicalDusk: DoubleArray,
        night: DoubleArray,
    ) = withMain {
        altitudeChart.draw(
            points, now,
            civilDawn, nauticalDawn, astronomicalDawn,
            civilDusk, nauticalDusk, astronomicalDusk,
            night,
        )
    }

    override suspend fun updateSunImage() = withMain {
        sunView.updateImage()
    }

    override suspend fun updateMoonImage(phase: Double, age: Double, angle: Angle) = withMain {
        moonView.draw(age, angle)
    }

    override suspend fun populatePlanet(planets: List<AtlasView.Planet>): Unit = withMain {
        planetTableView.items.setAll(planets)
    }

    override suspend fun populateMinorPlanet(minorPlanets: List<AtlasView.MinorPlanet>): Unit = withMain {
        minorPlanetTableView.items.setAll(minorPlanets)
    }

    override suspend fun populateStar(stars: List<AtlasView.Star>) = withMain {
        val filteredList = FilteredList(FXCollections.observableArrayList(stars))
        val sortedList = SortedList(filteredList)
        sortedList.comparatorProperty().bind(starTableView.comparatorProperty())
        starTableView.items = sortedList
    }

    override suspend fun populateDSOs(dsos: List<AtlasView.DSO>) = withMain {
        val filteredList = FilteredList(FXCollections.observableArrayList(dsos))
        val sortedList = SortedList(filteredList)
        sortedList.comparatorProperty().bind(dsosTableView.comparatorProperty())
        dsosTableView.items = sortedList
    }

    override suspend fun updateEquatorialCoordinates(
        ra: Angle, dec: Angle,
        raJ2000: Angle, decJ2000: Angle,
        constellation: Constellation?,
    ) = withMain {
        rightAscensionLabel.text = ra.format(AngleFormatter.HMS)
        declinationLabel.text = dec.format(AngleFormatter.SIGNED_DMS)
        rightAscensionJ2000Label.text = raJ2000.format(AngleFormatter.HMS)
        declinationJ2000Label.text = decJ2000.format(AngleFormatter.SIGNED_DMS)
        constellationLabel.text = constellation?.iau ?: "-"
    }

    override suspend fun updateHorizontalCoordinates(az: Angle, alt: Angle) = withMain {
        azimuthLabel.text = az.normalized.format(AngleFormatter.DMS)
        altitudeLabel.text = alt.format(AngleFormatter.SIGNED_DMS)
    }

    override suspend fun updateInfo(
        bodyName: String,
        extra: List<Pair<String, String>>,
    ) = withMain {
        nameLabel.text = bodyName

        for (i in extraPanes.indices) {
            if (i < extra.size) {
                extraPanes[i].isVisible = true
                extraPanes[i].text = extra[i].first
                extraLabels[i].text = extra[i].second
            } else {
                extraPanes[i].isVisible = false
            }
        }
    }

    override suspend fun updateRTS(rts: Triple<String, String, String>) = withMain {
        rtsLabel.text = "%s | %s | %s".format(rts.first, rts.second, rts.third)
    }

    override suspend fun clearAltitudeAndCoordinates() = withMain {
        nameLabel.text = ""
        updateEquatorialCoordinates(Angle.ZERO, Angle.ZERO, Angle.ZERO, Angle.ZERO, null)
        updateHorizontalCoordinates(Angle.ZERO, Angle.ZERO)
        altitudeChart.draw(emptyList())
    }

    private class MagnitudeTableCell<T> : TableCell<T, Double>() {

        override fun updateItem(item: Double?, empty: Boolean) {
            super.updateItem(item, empty)

            text = if (empty || item == null) null
            else if (item.isFinite() && item < 99.0) "%.1f".format(item)
            else "-"
        }
    }

    private class StarFilter(text: String) : Predicate<AtlasView.Star> {

        private val predicate = SkyCatalogFilter(text)

        override fun test(star: AtlasView.Star) = predicate.test(star.skyObject)
    }

    private class DSOFilter(text: String) : Predicate<AtlasView.DSO> {

        private val predicate = SkyCatalogFilter(text)

        override fun test(star: AtlasView.DSO) = predicate.test(star.skyObject)
    }
}
