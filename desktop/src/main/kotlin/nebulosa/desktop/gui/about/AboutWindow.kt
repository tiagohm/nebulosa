package nebulosa.desktop.gui.about

import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.about.AboutManager
import nebulosa.desktop.view.about.AboutView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class AboutWindow : AbstractWindow("About", "nebulosa-about"), AboutView {

    @Lazy @Autowired private lateinit var aboutManager: AboutManager

    init {
        title = "About"
        resizable = false
    }
}
