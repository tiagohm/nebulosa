package nebulosa.desktop.gui.about

import javafx.fxml.FXML
import javafx.scene.control.Label
import nebulosa.desktop.BuildConfig
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.about.AboutManager
import nebulosa.desktop.view.about.AboutView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class AboutWindow : AbstractWindow("About", "information"), AboutView {

    @Lazy @Autowired private lateinit var aboutManager: AboutManager

    @FXML private lateinit var versionLabel: Label
    @FXML private lateinit var builtOnLabel: Label

    init {
        title = "About"
        resizable = false
    }

    override fun onCreate() {
        versionLabel.text = "v${BuildConfig.VERSION_CODE} · ${BuildConfig.VERSION_NAME}"
        builtOnLabel.text = "Built on ${BuildConfig.BUILD_DATE}"
    }
}
