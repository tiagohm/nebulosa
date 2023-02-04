package nebulosa.desktop.gui.atlas

import javafx.event.Event
import javafx.fxml.FXML
import javafx.geometry.Point2D
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.AltitudeGraph
import nebulosa.desktop.logic.atlas.AtlasManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.atlas.Twilight
import nebulosa.math.Angle
import nebulosa.math.AngleFormatter
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.VSOP87E

class AtlasWindow : AbstractWindow(), AtlasView {

    data class Planet(
        val name: String,
        val type: String,
        val body: Body,
    )

    override val resourceName = "Atlas"

    override val icon = "nebulosa-atlas"

    @FXML private lateinit var ephemerisTabPane: TabPane
    @FXML private lateinit var rightAscensionLabel: Label
    @FXML private lateinit var declinationLabel: Label
    @FXML private lateinit var rightAscensionJ2000Label: Label
    @FXML private lateinit var declinationJ2000Label: Label
    @FXML private lateinit var altitudeLabel: Label
    @FXML private lateinit var azimuthLabel: Label
    @FXML private lateinit var altitudeGraph: AltitudeGraph
    @FXML private lateinit var sunImageView: ImageView
    @FXML private lateinit var moonImageView: ImageView
    @FXML private lateinit var planetTableView: TableView<Planet>

    @Volatile private var started = false

    private val atlasManager = AtlasManager(this)

    init {
        title = "Atlas"
        resizable = false
    }

    override fun onCreate() {
        planetTableView.columns[0].cellValueFactory = PropertyValueFactory<Planet, String>("name")
        planetTableView.columns[1].cellValueFactory = PropertyValueFactory<Planet, String>("type")

        planetTableView.items.add(Planet("Mercury", "Planet", VSOP87E.MERCURY))
        planetTableView.items.add(Planet("Venus", "Planet", VSOP87E.VENUS))
        planetTableView.items.add(Planet("Mars", "Planet", VSOP87E.MARS))
        planetTableView.items.add(Planet("Jupiter", "Planet", VSOP87E.JUPITER))
        planetTableView.items.add(Planet("Saturn", "Planet", VSOP87E.SATURN))
        planetTableView.items.add(Planet("Uranus", "Planet", VSOP87E.URANUS))
        planetTableView.items.add(Planet("Neptune", "Planet", VSOP87E.NEPTUNE))
        planetTableView.items.add(Planet("Pluto", "Dwarf Planet", VSOP87E.MERCURY))

        planetTableView.selectionModel.selectedItemProperty().on { if (it != null) atlasManager.computePlanet(it.body) }
    }

    override fun onStart() {
        started = true

        atlasManager.loadPreferences()

        atlasManager.updateSunImage()
        atlasManager.updateMoonImage()

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

    override fun drawAltitudeGraph(
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

    override fun updateEquatorialCoordinates(
        ra: Angle, dec: Angle,
        raJ2000: Angle, decJ2000: Angle,
    ) {
        rightAscensionLabel.text = ra.format(AngleFormatter.HMS)
        declinationLabel.text = dec.format(AngleFormatter.DMS)
        rightAscensionJ2000Label.text = raJ2000.format(AngleFormatter.HMS)
        declinationJ2000Label.text = decJ2000.format(AngleFormatter.DMS)
    }

    override fun updateHorizontalCoordinates(az: Angle, alt: Angle) {
        azimuthLabel.text = az.format(AngleFormatter.DMS)
        altitudeLabel.text = alt.format(AngleFormatter.DMS)
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
