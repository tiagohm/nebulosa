package nebulosa.desktop.gui.control

import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

fun interface CellTextFactory<S, T> : Callback<TableColumn<S, T>, TableCell<S, T>> {

    fun cell(item: S, value: T): String

    override fun call(param: TableColumn<S, T>) = object : TableCell<S, T>() {

        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            text = if (empty || item == null) null
            else cell(tableView.items[index], item)
        }
    }
}
