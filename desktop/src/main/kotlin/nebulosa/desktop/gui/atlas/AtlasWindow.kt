package nebulosa.desktop.gui.atlas

import javafx.event.Event
import javafx.fxml.FXML
import javafx.geometry.Point2D
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.AltitudeGraph
import nebulosa.desktop.logic.atlas.AtlasManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.atlas.Twilight
import nebulosa.math.Angle
import nebulosa.math.AngleFormatter

class AtlasWindow : AbstractWindow(), AtlasView {

    override val resourceName = "Atlas"

    override val icon = "nebulosa-atlas"

    @FXML private lateinit var ephemerisTabPane: TabPane
    @FXML private lateinit var rightAscensionLabel: Label
    @FXML private lateinit var declinationLabel: Label
    @FXML private lateinit var rightAscensionJ2000Label: Label
    @FXML private lateinit var declinationJ2000Label: Label
    @FXML private lateinit var altitudeLabel: Label
    @FXML private lateinit var azimuthLabel: Label
    @FXML private lateinit var sunImageView: ImageView
    @FXML private lateinit var moonImageView: ImageView
    @FXML private lateinit var planetTableView: TableView<AtlasView.Planet>
    @FXML private lateinit var searchMinorPlanetTextField: TextField
    @FXML private lateinit var minorPlanetTableView: TableView<AtlasView.MinorPlanet>
    @FXML private lateinit var goToButton: Button
    @FXML private lateinit var slewToButton: Button
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var altitudeGraph: AltitudeGraph

    @Volatile private var started = false

    private val atlasManager = AtlasManager(this)

    init {
        title = "Atlas"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = !atlasManager.mountProperty.connectedProperty
        val isMoving = atlasManager.mountProperty.slewingProperty or atlasManager.mountProperty.parkingProperty

        goToButton.disableProperty().bind(atlasManager.mountProperty.isNull or isNotConnected or isMoving)

        slewToButton.disableProperty().bind(goToButton.disableProperty())

        syncButton.disableProperty().bind(goToButton.disableProperty())

        planetTableView.columns[0].cellValueFactory = PropertyValueFactory<AtlasView.Planet, String>("name")
        planetTableView.columns[1].cellValueFactory = PropertyValueFactory<AtlasView.Planet, String>("type")
        planetTableView.selectionModel.selectedItemProperty().on { if (it != null) atlasManager.computePlanet(it.body) }

        minorPlanetTableView.columns[0].cellValueFactory = PropertyValueFactory<AtlasView.MinorPlanet, String>("element")
        minorPlanetTableView.columns[1].cellValueFactory = PropertyValueFactory<AtlasView.MinorPlanet, String>("description")
        minorPlanetTableView.columns[2].cellValueFactory = PropertyValueFactory<AtlasView.MinorPlanet, String>("value")
    }

    override fun onStart() {
        started = true

        atlasManager.loadPreferences()

        atlasManager.updateSunImage()
        atlasManager.updateMoonImage()
        atlasManager.populatePlanets()

        atlasManager.computeTab(AtlasView.TabType.SUN)
    }

    override fun onStop() {
        atlasManager.savePreferences()
    }

    override fun onClose() {
        atlasManager.close()
    }

    @FXML
    private fun tabSelectionChanged(event: Event) {
        if (!started) return
        if (!(event.source as Tab).isSelected) return
        val userData = ephemerisTabPane.selectionModel.selectedItem.userData as String
        val tabType = AtlasView.TabType.valueOf(userData)
        atlasManager.computeTab(tabType)
    }

    @FXML
    private fun goTo() {
        atlasManager.goTo()
    }

    @FXML
    private fun slewTo() {
        atlasManager.slewTo()
    }

    @FXML
    private fun sync() {
        atlasManager.sync()
    }

    @FXML
    private fun searchMinorPlanet() {
        val text = searchMinorPlanetTextField.text.trim().ifEmpty { null } ?: return
        atlasManager.searchAsteroidsAndComets(text)
    }

    override fun drawAltitude(
        points: List<Point2D>, now: Double,
        civilTwilight: Twilight, nauticalTwilight: Twilight, astronomicalTwilight: Twilight,
    ) {
        altitudeGraph.draw(points, now, civilTwilight, nauticalTwilight, astronomicalTwilight)
    }

    override fun updateSunImage(uri: String) {
        sunImageView.image = Image(uri)
    }

    override fun updateMoonImage(uri: String) {
        moonImageView.image = Image(uri)
    }

    override fun populatePlanets(planets: List<AtlasView.Planet>) {
        planetTableView.items.setAll(planets)
    }

    override fun populateMinorPlanet(minorPlanet: List<AtlasView.MinorPlanet>) {
        minorPlanetTableView.items.setAll(minorPlanet)
    }

    override fun updateEquatorialCoordinates(
        ra: Angle, dec: Angle,
        raJ2000: Angle, decJ2000: Angle,
    ) {
        rightAscensionLabel.text = ra.format(AngleFormatter.HMS)
        declinationLabel.text = dec.format(AngleFormatter.SIGNED_DMS)
        rightAscensionJ2000Label.text = raJ2000.format(AngleFormatter.HMS)
        declinationJ2000Label.text = decJ2000.format(AngleFormatter.SIGNED_DMS)
    }

    override fun updateHorizontalCoordinates(az: Angle, alt: Angle) {
        azimuthLabel.text = az.normalized.format(AngleFormatter.DMS)
        altitudeLabel.text = alt.format(AngleFormatter.SIGNED_DMS)
    }

    override fun clearAltitudeAndCoordinates() {
        updateEquatorialCoordinates(Angle.ZERO, Angle.ZERO, Angle.ZERO, Angle.ZERO)
        updateHorizontalCoordinates(Angle.ZERO, Angle.ZERO)
        altitudeGraph.draw(emptyList())
    }

    companion object {

        @JvmStatic private var window: AtlasWindow? = null

        @JvmStatic
        fun open() {
            if (window == null) window = AtlasWindow()
            window!!.show(bringToFront = true)
        }
    }
}
