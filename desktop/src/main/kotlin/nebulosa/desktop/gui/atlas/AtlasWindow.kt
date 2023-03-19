package nebulosa.desktop.gui.atlas

import eu.hansolo.fx.charts.*
import eu.hansolo.fx.charts.data.XYItem
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.util.Callback
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.AltitudeChart
import nebulosa.desktop.gui.control.CopyableLabel
import nebulosa.desktop.gui.control.MoonView
import nebulosa.desktop.gui.control.SunView
import nebulosa.desktop.logic.atlas.AtlasManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.math.Angle
import nebulosa.math.AngleFormatter
import nebulosa.math.PairOfAngle
import nebulosa.nova.astrometry.Constellation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

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
    @FXML private lateinit var dsoTableView: TableView<AtlasView.DSO>
    @FXML private lateinit var goToButton: Button
    @FXML private lateinit var slewToButton: Button
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var frameButton: Button
    @FXML private lateinit var altitudeChart: AltitudeChart

    @Volatile private var started = false

    init {
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = !atlasManager.mountProperty.connectedProperty
        val isMoving = atlasManager.mountProperty.slewingProperty or atlasManager.mountProperty.parkingProperty
        val isComputing = atlasManager.computing

        atlasManager.initialize()

        ephemerisTabPane.disableProperty().bind(isComputing)

        goToButton.disableProperty().bind(atlasManager.mountProperty.isNull or isNotConnected or isMoving or isComputing)

        slewToButton.disableProperty().bind(goToButton.disableProperty())

        syncButton.disableProperty().bind(goToButton.disableProperty())

        frameButton.disableProperty().bind(isComputing)

        planetTableView.columns[0].cellValueFactory = PropertyValueFactory<AtlasView.Planet, String>("name")
        planetTableView.columns[1].cellValueFactory = PropertyValueFactory<AtlasView.Planet, String>("type")
        planetTableView.selectionModel.selectedItemProperty().on { if (it != null) atlasManager.computePlanet(it) }

        minorPlanetTableView.columns[0].cellValueFactory = PropertyValueFactory<AtlasView.MinorPlanet, String>("element")
        minorPlanetTableView.columns[1].cellValueFactory = PropertyValueFactory<AtlasView.MinorPlanet, String>("description")
        minorPlanetTableView.columns[2].cellValueFactory = PropertyValueFactory<AtlasView.MinorPlanet, String>("value")

        starTableView.columns[0].cellValueFactory = PropertyValueFactory<AtlasView.Star, String>("name")
        starTableView.columns[1].cellValueFactory = PropertyValueFactory<AtlasView.Star, Double>("magnitude")
        starTableView.columns[1].cellFactory = Callback { _ -> MagnitudeTableCell<AtlasView.Star>() }
        starTableView.columns[2].cellValueFactory = PropertyValueFactory<AtlasView.Star, String>("type")
        starTableView.selectionModel.selectedItemProperty().on { if (it != null) atlasManager.computeStar(it) }

        dsoTableView.columns[0].cellValueFactory = PropertyValueFactory<AtlasView.DSO, String>("name")
        dsoTableView.columns[1].cellValueFactory = PropertyValueFactory<AtlasView.DSO, Double>("magnitude")
        dsoTableView.columns[1].cellFactory = Callback { _ -> MagnitudeTableCell<AtlasView.DSO>() }
        dsoTableView.columns[2].cellValueFactory = PropertyValueFactory<AtlasView.DSO, String>("type")
        dsoTableView.selectionModel.selectedItemProperty().on { if (it != null) atlasManager.computeDSO(it) }
    }

    override fun onStart() {
        started = true

        atlasManager.loadPreferences()

        atlasManager.updateSunImage()
        atlasManager.updateMoonImage()
        atlasManager.populatePlanets()
        atlasManager.populateStars()

        atlasManager.computeTab(AtlasView.TabType.SUN)
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
        atlasManager.computeTab(tabType)
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
        atlasManager.frame(ra, dec)
    }

    @FXML
    private fun searchMinorPlanet() {
        val text = searchMinorPlanetTextField.text.trim().ifEmpty { null } ?: return
        atlasManager.searchMinorPlanet(text)
    }

    @FXML
    @Suppress("UNCHECKED_CAST")
    private fun searchStar() {
        val text = searchStarTextField.text.trim()
        ((starTableView.items as SortedList<*>).source as FilteredList<AtlasView.Star>)
            .setPredicate { text.isBlank() || it.name.contains(text, true) }
    }

    @FXML
    private fun searchDSO() {
        val text = searchDSOTextField.text.trim().ifEmpty { null } ?: return
        atlasManager.searchDSO(text)
    }

    override fun drawAltitude(
        points: List<XYItem>,
        now: Double,
        civilDawn: DoubleArray, nauticalDawn: DoubleArray, astronomicalDawn: DoubleArray,
        civilDusk: DoubleArray, nauticalDusk: DoubleArray, astronomicalDusk: DoubleArray,
        night: DoubleArray,
    ) {
        altitudeChart.draw(
            points, now,
            civilDawn, nauticalDawn, astronomicalDawn,
            civilDusk, nauticalDusk, astronomicalDusk,
            night,
        )
    }

    override fun updateSunImage() {
        sunView.updateImage()
    }

    override fun updateMoonImage(phase: Double, age: Double, angle: Angle) {
        moonView.draw(age, angle)
    }

    override fun populatePlanet(planets: List<AtlasView.Planet>) {
        planetTableView.items.setAll(planets)
    }

    override fun populateMinorPlanet(minorPlanets: List<AtlasView.MinorPlanet>) {
        minorPlanetTableView.items.setAll(minorPlanets)
    }

    override fun populateStar(stars: List<AtlasView.Star>) {
        val filteredList = FilteredList(FXCollections.observableArrayList(stars))
        val sortedList = SortedList(filteredList)
        sortedList.comparatorProperty().bind(starTableView.comparatorProperty())
        starTableView.items = sortedList
    }

    override fun populateDSO(dso: List<AtlasView.DSO>) {
        dsoTableView.items.setAll(dso)

        if (dso.size == 1) {
            dsoTableView.selectionModel.selectFirst()
        }
    }

    override fun updateEquatorialCoordinates(
        ra: Angle, dec: Angle,
        raJ2000: Angle, decJ2000: Angle,
        constellation: Constellation?,
    ) {
        rightAscensionLabel.text = ra.format(AngleFormatter.HMS)
        declinationLabel.text = dec.format(AngleFormatter.SIGNED_DMS)
        rightAscensionJ2000Label.text = raJ2000.format(AngleFormatter.HMS)
        declinationJ2000Label.text = decJ2000.format(AngleFormatter.SIGNED_DMS)
        constellationLabel.text = constellation?.iau ?: "-"
    }

    override fun updateHorizontalCoordinates(az: Angle, alt: Angle) {
        azimuthLabel.text = az.normalized.format(AngleFormatter.DMS)
        altitudeLabel.text = alt.format(AngleFormatter.SIGNED_DMS)
    }

    override fun updateInfo(bodyName: String) {
        nameLabel.text = bodyName
    }

    override fun updateRTS(rts: Triple<String, String, String>) {
        rtsLabel.text = "%s | %s | %s".format(rts.first, rts.second, rts.third)
    }

    override fun clearAltitudeAndCoordinates() {
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
}
