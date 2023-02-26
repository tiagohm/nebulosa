package nebulosa.desktop.gui.framing

import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.framing.FramingManager
import nebulosa.desktop.view.framing.FramingView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class FramingWindow : AbstractWindow("Framing"), FramingView {

    @Lazy @Autowired private lateinit var framingManager: FramingManager

    init {
        title = "Framing"
        resizable = false
    }
}
