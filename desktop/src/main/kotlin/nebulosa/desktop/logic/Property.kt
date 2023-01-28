package nebulosa.desktop.logic

import javafx.beans.property.Property
import javafx.beans.value.ObservableObjectValue
import javafx.beans.value.WritableObjectValue
import java.io.Closeable

interface Property<T> : Property<T>, ObservableObjectValue<T>, WritableObjectValue<T>, Closeable
