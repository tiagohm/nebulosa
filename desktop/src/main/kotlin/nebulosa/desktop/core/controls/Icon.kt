package nebulosa.desktop.core.controls

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import nebulosa.io.resource

class Icon(name: String) : ImageView(Image(resource("icons/$name.png"), 24.0, 24.0, true, true)) {

    companion object {

        @JvmStatic
        fun closeCircle() = Icon("close-circle")

        @JvmStatic
        fun connection() = Icon("connection")
    }
}
