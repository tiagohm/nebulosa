package nebulosa.desktop.gui.control

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn
import javafx.util.Callback
import java.util.function.Function

fun interface PropertyValueFactory<S, T> : Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>>, Function<S, T> {

    override fun call(param: TableColumn.CellDataFeatures<S, T>) = ReadOnlyObjectWrapper(apply(param.value))
}
