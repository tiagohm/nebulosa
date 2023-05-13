package nebulosa.desktop.gui.control

import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import nebulosa.io.resource
import java.io.InputStreamReader

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

        @JvmStatic private val CSV_READER = CsvReader.builder()
            .fieldSeparator(';')
            .quoteCharacter('"')
            .commentStrategy(CommentStrategy.SKIP)
            .commentCharacter('#')
            .skipEmptyRows(true)

        init {
            with(HashMap<String, String>(7296)) {
                resource("data/MaterialDesignIcon.csv")!!.use { stream ->
                    for (row in CSV_READER.build(InputStreamReader(stream))) {
                        this[row.getField(0)] = row.getField(1)
                    }
                }

                ICONS = this
            }
        }
    }
}
