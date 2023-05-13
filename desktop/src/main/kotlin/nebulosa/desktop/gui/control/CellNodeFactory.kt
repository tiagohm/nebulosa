package nebulosa.desktop.gui.control

import javafx.scene.Node
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

fun interface CellNodeFactory<S, T, N : Node> : Callback<TableColumn<S, T>, TableCell<S, T>> {

    fun cell(item: S, value: T?, node: N?): N

    fun dispose(node: N) {}

    override fun call(param: TableColumn<S, T>) = object : TableCell<S, T>() {

        @Suppress("UNCHECKED_CAST")
        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            val node = graphic as? N

            if (empty) {
                node?.also(::dispose)
                graphic = null
                text = null
            } else {
                graphic = cell(tableView.items[index], item, node)
                text = null
            }
        }
    }
}
