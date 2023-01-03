package nebulosa.desktop.core.controls

import javafx.scene.Node
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

interface ButtonValueFactory<S, T> : Callback<TableColumn<S, T>, TableCell<S, T>> {

    fun cell(item: S, node: Node?): Node

    fun dispose(node: Node)

    override fun call(param: TableColumn<S, T>) = object : TableCell<S, T>() {

        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty) {
                if (graphic != null) dispose(graphic)
                graphic = null
                text = null
            } else {
                graphic = cell(tableView.items[index], graphic)
                text = null
            }
        }
    }
}
