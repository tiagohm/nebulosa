package nebulosa.desktop.gui.atlas

import eu.hansolo.fx.charts.data.XYItem
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.util.Callback
import javafx.util.converter.LocalDateStringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.CopyableLabel
import nebulosa.desktop.gui.control.LabeledPane
import nebulosa.desktop.gui.control.PropertyValueFactory
import nebulosa.desktop.gui.control.SwitchSegmentedButton
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.atlas.AtlasManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.service.SkyObjectService
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.math.Angle
import nebulosa.math.AngleFormatter
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.m
import nebulosa.math.PairOfAngle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObject.Companion.NAME_SEPARATOR
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.time.*
import java.time.format.DateTimeFormatter

@Component
class AtlasWindow : AbstractWindow("Atlas", "sky"), AtlasView, AltitudeChart.NowListener {

    @Lazy @Autowired private lateinit var atlasManager: AtlasManager

    @FXML private lateinit var ephemerisTabPane: TabPane
    @FXML private lateinit var nameLabel: CopyableLabel
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
    @FXML private lateinit var starTableView: TableView<SkyObject>
    @FXML private lateinit var searchDSOTextField: TextField
    @FXML private lateinit var dsosTableView: TableView<SkyObject>
    @FXML private lateinit var goToButton: Button
    @FXML private lateinit var slewToButton: Button
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var frameButton: Button
    @FXML private lateinit var altitudeChart: AltitudeChart
    @FXML private lateinit var latitudeTextField: TextField
    @FXML private lateinit var longitudeTextField: TextField
    @FXML private lateinit var elevationTextField: TextField
    @FXML private lateinit var useCoordinatesFromMountSwitch: SwitchSegmentedButton
    @FXML private lateinit var dateDatePicker: DatePicker

    @Volatile private var started = false

    private lateinit var extraLabels: Array<CopyableLabel>
    private lateinit var extraPanes: Array<LabeledPane>

    private val searchStarFilterWindow by lazy { beanFactory.createBean(SkyObjectFilterWindow::class.java) }
    private val searchDSOFilterWindow by lazy { beanFactory.createBean(SkyObjectFilterWindow::class.java) }

    init {
        resizable = false
        title = "Atlas"
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate() {
        extraLabels = arrayOf(extra1Label, extra2Label, extra3Label, extra4Label)
        extraPanes = Array(extraLabels.size) { extraLabels[it].parent as LabeledPane }

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
            .on { if (it != null) launch { atlasManager.computeBody(AtlasView.TabType.PLANET, it) } }

        (minorPlanetTableView.columns[0] as TableColumn<AtlasView.MinorPlanet, String>).cellValueFactory = PropertyValueFactory { it.element }
        (minorPlanetTableView.columns[1] as TableColumn<AtlasView.MinorPlanet, String>).cellValueFactory = PropertyValueFactory { it.description }
        (minorPlanetTableView.columns[2] as TableColumn<AtlasView.MinorPlanet, String>).cellValueFactory = PropertyValueFactory { it.value }

        (starTableView.columns[0] as TableColumn<SkyObject, String>).cellValueFactory = PropertyValueFactory { it.names.firstName() }
        (starTableView.columns[1] as TableColumn<SkyObject, Double>).cellValueFactory = PropertyValueFactory { it.magnitude }
        (starTableView.columns[1] as TableColumn<SkyObject, Double>).cellFactory = Callback { _ -> MagnitudeTableCell() }
        (starTableView.columns[2] as TableColumn<SkyObject, String>).cellValueFactory = PropertyValueFactory { it.type.description }
        (starTableView.columns[3] as TableColumn<SkyObject, String>).cellValueFactory = PropertyValueFactory { it.constellation.iau }
        starTableView.selectionModel.selectedItemProperty().on { if (it != null) launch { atlasManager.computeBody(AtlasView.TabType.STAR, it) } }

        (dsosTableView.columns[0] as TableColumn<SkyObject, String>).cellValueFactory = PropertyValueFactory { it.names.firstName() }
        (dsosTableView.columns[1] as TableColumn<SkyObject, Double>).cellValueFactory = PropertyValueFactory { it.magnitude }
        (dsosTableView.columns[1] as TableColumn<SkyObject, Double>).cellFactory = Callback { _ -> MagnitudeTableCell() }
        (dsosTableView.columns[2] as TableColumn<SkyObject, String>).cellValueFactory = PropertyValueFactory { it.type.description }
        (dsosTableView.columns[3] as TableColumn<SkyObject, String>).cellValueFactory = PropertyValueFactory { it.constellation.iau }
        dsosTableView.selectionModel.selectedItemProperty().on { if (it != null) launch { atlasManager.computeBody(AtlasView.TabType.DSO, it) } }

        dateDatePicker.converter = LocalDateStringConverter(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ISO_LOCAL_DATE)
        dateDatePicker.value = LocalDate.now()

        altitudeChart.registerNowListener(this)

        launch { atlasManager.populatePlanets() }
    }

    override fun onStart() {
        started = true

        atlasManager.loadPreferences()

        launch { atlasManager.updateSunImage() }
        launch { atlasManager.updateMoonImage() }
        launch { atlasManager.computeTab(AtlasView.TabType.SUN) }
    }

    override fun onStop() {
        atlasManager.savePreferences()
    }

    override val latitude
        get() = Angle.from(latitudeTextField.text) ?: Angle.ZERO

    override val longitude
        get() = Angle.from(longitudeTextField.text) ?: Angle.ZERO

    override val elevation
        get() = elevationTextField.text?.toDoubleOrNull()?.m ?: Distance.ZERO

    override val date: LocalDate
        get() = dateDatePicker.value ?: LocalDate.now()

    override val time
        get() = altitudeChart.now

    override val manualMode
        get() = altitudeChart.manualMode

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
    private fun searchStar() {
        val text = searchStarTextField.text.trim()
        launch { atlasManager.searchStar(text) }
    }

    @FXML
    private fun openFilterStar() {
        searchStarFilterWindow.showAndWait(this) {
            if (searchStarFilterWindow.filtered) {
                val text = searchStarTextField.text.trim()
                val filter = SkyObjectService.Filter(
                    searchStarFilterWindow.rightAscension,
                    searchStarFilterWindow.declination,
                    searchStarFilterWindow.radius,
                    searchStarFilterWindow.constellation,
                    searchStarFilterWindow.mangitudeMin,
                    searchStarFilterWindow.magnitudeMax,
                    searchStarFilterWindow.type,
                )

                launch { atlasManager.searchStar(text, filter) }
            }
        }
    }

    @FXML
    private fun searchDSO() {
        val text = searchDSOTextField.text.trim()
        launch { atlasManager.searchDSO(text) }
    }

    @FXML
    private fun openFilterDSO() {
        searchDSOFilterWindow.showAndWait(this) {
            if (searchDSOFilterWindow.filtered) {
                val text = searchDSOTextField.text.trim()
                val filter = SkyObjectService.Filter(
                    searchDSOFilterWindow.rightAscension,
                    searchDSOFilterWindow.declination,
                    searchDSOFilterWindow.radius,
                    searchDSOFilterWindow.constellation,
                    searchDSOFilterWindow.mangitudeMin,
                    searchDSOFilterWindow.magnitudeMax,
                    searchDSOFilterWindow.type,
                )

                launch { atlasManager.searchDSO(text, filter) }
            }
        }
    }

    @FXML
    private fun applySettings() {
        atlasManager.applySettings(useCoordinatesFromMountSwitch.state)
    }

    override suspend fun drawPoints(points: List<XYItem>) = withMain {
        altitudeChart.drawPoints(points)
    }

    override suspend fun drawNow() = withMain {
        altitudeChart.drawNow()
    }

    override suspend fun drawTwilight(
        civilDawn: DoubleArray, nauticalDawn: DoubleArray, astronomicalDawn: DoubleArray,
        civilDusk: DoubleArray, nauticalDusk: DoubleArray, astronomicalDusk: DoubleArray,
        night: DoubleArray,
    ) = withMain {
        altitudeChart.drawTwilight(
            civilDawn, nauticalDawn, astronomicalDawn,
            civilDusk, nauticalDusk, astronomicalDusk,
            night,
        )
    }

    override suspend fun updateSunImage(image: BufferedImage) {
        sunView.updateImage(image)
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

    override suspend fun populateStar(stars: List<SkyObject>) = withMain {
        val filteredList = FilteredList(FXCollections.observableArrayList(stars))
        val sortedList = SortedList(filteredList)
        sortedList.comparatorProperty().bind(starTableView.comparatorProperty())
        starTableView.items = sortedList
    }

    override suspend fun populateDSOs(dsos: List<SkyObject>) = withMain {
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
        constellationLabel.text = constellation?.let { "%s (%s)".format(it.latinName, it.iau) } ?: "-"
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
        altitudeChart.drawPoints(emptyList())
    }

    override fun loadCoordinates(useCoordinatesFromMount: Boolean, latitude: Angle, longitude: Angle, elevation: Distance) {
        useCoordinatesFromMountSwitch.state = useCoordinatesFromMount
        latitudeTextField.text = latitude.format(AngleFormatter.SIGNED_DMS)
        longitudeTextField.text = longitude.format(AngleFormatter.SIGNED_DMS)
        elevationTextField.text = "%.1f".format(elevation.meters)
    }

    override fun onNowChanged(time: LocalTime, manual: Boolean) {
        val dateTime = OffsetDateTime.of(date, time, ZoneOffset.UTC).atZoneSameInstant(ZoneId.systemDefault())
        title = "Atlas · %s %s".format(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE), dateTime.format(DateTimeFormatter.ISO_LOCAL_TIME))

        if (manual) {
            launch { atlasManager.computeTab() }
        }
    }

    private class MagnitudeTableCell : TableCell<SkyObject, Double>() {

        override fun updateItem(item: Double?, empty: Boolean) {
            super.updateItem(item, empty)

            text = if (empty || item == null) null
            else if (item.isFinite() && item < SkyObject.UNKNOWN_MAGNITUDE) "%.1f".format(item)
            else "-"
        }
    }

    companion object {

        @JvmStatic
        private fun String.firstName() = indexOf(NAME_SEPARATOR).let { if (it < 0) this else substring(0, it) }
    }
}
