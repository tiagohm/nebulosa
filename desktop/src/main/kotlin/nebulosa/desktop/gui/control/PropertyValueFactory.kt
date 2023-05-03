package nebulosa.desktop.gui.control

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn
import javafx.util.Callback

open class PropertyValueFactory<S, T>(private val callback: (S) -> T) : Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {

    final override fun call(param: TableColumn.CellDataFeatures<S, T>) = ReadOnlyObjectWrapper(callback(param.value))
}
