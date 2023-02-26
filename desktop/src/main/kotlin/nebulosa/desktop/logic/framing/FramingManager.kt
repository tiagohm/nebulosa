package nebulosa.desktop.logic.framing

import nebulosa.desktop.view.framing.FramingView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FramingManager(@Autowired private val view: FramingView) {
}
