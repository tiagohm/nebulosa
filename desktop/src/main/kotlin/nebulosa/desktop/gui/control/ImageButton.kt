package nebulosa.desktop.gui.control

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class ImageButton : Button() {

    init {
        alignment = Pos.CENTER
        isMnemonicParsing = false
    }

    var image: Image?
        get() = (graphic as? ImageView)?.image
        set(value) {
            if (value != null) {
                if (graphic !is ImageView) {
                    val imageView = ImageView(value)
                    imageView.fitWidth = size
                    imageView.fitHeight = size
                    imageView.isPreserveRatio = true
                    graphic = imageView
                } else {
                    (graphic as ImageView).image = value
                }
            } else {
                graphic = null
            }
        }

    var url
        get() = image?.url
        set(value) {
            image = if (value == null) null else Image(value)
        }

    var size = 16.0
        set(value) {
            field = value
            val image = (graphic as? ImageView) ?: return
            image.fitWidth = size
            image.fitHeight = size
        }
}
