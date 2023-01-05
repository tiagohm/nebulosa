package nebulosa.desktop.core.beans

import javafx.beans.binding.Bindings
import javafx.beans.value.*

// Any.

infix fun <E> ObservableObjectValue<E>.transformed(action: (E) -> String): ObservableStringValue {
    return Bindings.createStringBinding({ action(value) }, this)
}

// Double.

infix fun ObservableDoubleValue.transformed(action: (Double) -> String): ObservableStringValue {
    return Bindings.createStringBinding({ action(value.toDouble()) }, this)
}

// List.

infix fun <E> ObservableListValue<E>.contains(element: E): ObservableBooleanValue {
    return Bindings.createBooleanBinding({ element in this }, this)
}

infix fun <E> ObservableListValue<E>.notContains(element: E): ObservableBooleanValue {
    return Bindings.createBooleanBinding({ element !in this }, this)
}

// Boolean.

infix fun ObservableBooleanValue.and(other: ObservableBooleanValue): ObservableBooleanValue {
    return Bindings.and(this, other)
}

infix fun ObservableBooleanValue.or(other: ObservableBooleanValue): ObservableBooleanValue {
    return Bindings.or(this, other)
}

infix fun ObservableBooleanValue.xor(other: ObservableBooleanValue): ObservableBooleanValue {
    return Bindings.notEqual(this, other)
}
