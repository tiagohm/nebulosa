package nebulosa.desktop.gui.control

import javafx.beans.NamedArg
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class IconButton(@NamedArg("size") size: Double = DEFAULT_SIZE) : Button() {

    init {
        alignment = Pos.CENTER
        isMnemonicParsing = false
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
        minHeight = size
        maxHeight = size
        prefHeight = size
        minWidth = size
        maxWidth = size
        prefWidth = size
    }

    var image: Image?
        get() = (graphic as? ImageView)?.image
        set(value) {
            if (graphic !is ImageView) {
                val imageView = ImageView(value)
                imageView.fitWidth = size / 2 - 4.0
                imageView.fitHeight = size / 2 - 4.0
                imageView.isPreserveRatio = true
                graphic = imageView
            } else {
                (graphic as ImageView).image = value
            }
        }

    var size = DEFAULT_SIZE
        set(value) {
            field = value
            minHeight = value
            maxHeight = value
            prefHeight = value
            minWidth = value
            maxWidth = value
            prefWidth = value
        }

    companion object {

        const val DEFAULT_SIZE = 38.0
    }
}
