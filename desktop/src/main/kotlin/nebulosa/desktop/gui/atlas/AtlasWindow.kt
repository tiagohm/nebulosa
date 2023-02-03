package nebulosa.desktop.gui.atlas

import javafx.event.Event
import javafx.fxml.FXML
import javafx.geometry.Point2D
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.AltitudeGraph
import nebulosa.desktop.logic.atlas.AtlasManager
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.atlas.Twilight

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
    @FXML private lateinit var altitudeGraph: AltitudeGraph
    @FXML private lateinit var sunImageView: ImageView

    @Volatile private var started = false

    private val atlasManager = AtlasManager(this)

    init {
        title = "Atlas"
        resizable = false
    }

    override fun onStart() {
        started = true

        atlasManager.loadPreferences()

        atlasManager.updateSunImage()
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

    override fun updateSunImage(path: String) {
        sunImageView.image = Image("file://$path")
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
