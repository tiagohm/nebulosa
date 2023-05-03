package nebulosa.desktop.gui.control

import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import nebulosa.io.resource

class MaterialIcon : AnchorPane() {

    private val child = Label()

    private var currentIcon = ""

    init {
        child.alignment = Pos.CENTER
        child.contentDisplay = ContentDisplay.LEFT
        setTopAnchor(child, 0.0)
        setBottomAnchor(child, 0.0)
        setLeftAnchor(child, 0.0)
        setRightAnchor(child, 0.0)
        children.add(child)
    }

    var icon
        get() = currentIcon
        set(value) {
            currentIcon = ICONS[value] ?: value
            val label = child.graphic as? Label ?: Label()
            label.alignment = Pos.CENTER
            label.styleClass.setAll("mdi", "mdi-$size", "text-$color")
            label.text = currentIcon
            child.graphic = label
        }

    var text
        get() = child.text ?: ""
        set(value) {
            child.text = value
        }

    var size = "sm"
        set(value) {
            (child.graphic as? Label)?.apply {
                styleClass.remove("mdi-$field")
                styleClass.add("mdi-$value")
            }

            field = value
        }

    var color = "accent"
        set(value) {
            (child.graphic as? Label)?.apply {
                styleClass.remove("text-$field")
                styleClass.add("text-$value")
            }

            field = value
        }

    companion object {

        @JvmStatic private val ICONS: Map<String, String>

        init {
            with(HashMap<String, String>(7296)) {
                for (line in resource("data/MaterialDesignIcon.csv")!!.bufferedReader().lines()) {
                    if (line.isEmpty()) continue
                    val nameAndCode = line.split(";")
                    this[nameAndCode[0]] = nameAndCode[1]
                }

                ICONS = this
            }
        }
    }
}
