package nebulosa.desktop.core.controls

import javafx.scene.Node
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

fun interface ButtonValueFactory<S, T> : Callback<TableColumn<S, T>, TableCell<S, T>> {

    fun getCell(item: S, node: Node?): Node

    override fun call(param: TableColumn<S, T>) = object : TableCell<S, T>() {

        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty) {
                graphic = null
                text = null
            } else {
                graphic = getCell(tableView.items[index], graphic)
                text = null
            }
        }
    }
}
