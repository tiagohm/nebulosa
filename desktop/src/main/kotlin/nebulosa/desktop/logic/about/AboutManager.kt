package nebulosa.desktop.logic.about

import nebulosa.desktop.view.about.AboutView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AboutManager(@Autowired internal val view: AboutView)
