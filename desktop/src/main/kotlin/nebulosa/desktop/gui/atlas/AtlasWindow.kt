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
        rightAscensionLabel.text = Angle.formatHMS(ra, RA_FORMAT)
        declinationLabel.text = Angle.formatDMS(dec, DEC_FORMAT)
        rightAscensionJ2000Label.text = Angle.formatHMS(raJ2000, RA_FORMAT)
        declinationJ2000Label.text = Angle.formatDMS(decJ2000, DEC_FORMAT)
    }

    override fun updateHorizontalCoordinates(az: Angle, alt: Angle) {
        azimuthLabel.text = Angle.formatDMS(az, AZ_FORMAT)
        altitudeLabel.text = Angle.formatDMS(alt, ALT_FORMAT)
    }

    companion object {

        private const val RA_FORMAT = "%02dh %02dm %05.02fs"
        private const val DEC_FORMAT = "%s%02d° %02d' %05.02f\""
        private const val AZ_FORMAT = "%2$03d° %3$02d' %4$05.02f\""
        private const val ALT_FORMAT = "%s%02d° %02d' %05.02f\""

        @JvmStatic private var window: AtlasWindow? = null

        @JvmStatic
        fun open() {
            if (window == null) window = AtlasWindow()
            window!!.show(bringToFront = true)
        }
    }
}
