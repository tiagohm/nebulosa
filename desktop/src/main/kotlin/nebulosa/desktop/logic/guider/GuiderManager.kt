package nebulosa.desktop.logic.guider

import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.view.guider.GuiderSettingsView
import nebulosa.desktop.view.guider.GuiderType
import nebulosa.desktop.view.guider.GuiderView
import nebulosa.guiding.Guider
import nebulosa.guiding.phd2.PHD2Guider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GuiderManager(@Autowired internal val view: GuiderView) {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var guiderSettingsView: GuiderSettingsView

    @Volatile private var guider: Guider? = null

    val connectingProperty = SimpleBooleanProperty()
    val connectedProperty = SimpleBooleanProperty()

    val connected
        get() = connectedProperty.get()

    fun connect(type: GuiderType) {
        if (connected) {
            guider?.close()
            guider = null

            connectedProperty.set(false)

            view.title = "Guider"
        } else {
            val host = preferences.string("phd2.host") ?: "localhost"
            val port = preferences.string("phd2.port")?.toIntOrNull() ?: 4400

            val guider = when (type) {
                GuiderType.PHD2 -> PHD2Guider(host, port)
            }

            try {
                connectingProperty.set(true)
                guider.connect()
                connectedProperty.set(true)

                view.title = "Guider · $type"
            } catch (e: Throwable) {
                LOG.error("connection error", e)

                view.showAlert("Unable to connect to $host:$port. ${e.message}")
            } finally {
                connectingProperty.set(false)
            }
        }
    }

    fun openPHD2() {
        guiderSettingsView.show(bringToFront = true)
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuiderManager::class.java)
    }
}
