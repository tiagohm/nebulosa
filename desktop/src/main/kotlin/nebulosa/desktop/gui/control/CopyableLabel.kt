package nebulosa.desktop.gui.control

import javafx.scene.control.TextField

class CopyableLabel : TextField() {

    init {
        styleClass.add("copyable-label")
        maxWidth = Double.POSITIVE_INFINITY
        isEditable = false
    }
}
