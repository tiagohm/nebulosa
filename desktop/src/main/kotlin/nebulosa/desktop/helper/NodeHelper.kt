package nebulosa.desktop.helper

import javafx.scene.Node
import javafx.scene.layout.AnchorPane

fun Node.anchor(
    top: Double? = AnchorPane.getTopAnchor(this),
    right: Double? = AnchorPane.getRightAnchor(this),
    bottom: Double? = AnchorPane.getBottomAnchor(this),
    left: Double? = AnchorPane.getLeftAnchor(this),
) {
    AnchorPane.setTopAnchor(this, top)
    AnchorPane.setRightAnchor(this, right)
    AnchorPane.setBottomAnchor(this, bottom)
    AnchorPane.setLeftAnchor(this, left)
}
