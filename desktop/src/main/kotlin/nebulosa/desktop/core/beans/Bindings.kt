@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.desktop.core.beans

import javafx.beans.binding.Bindings
import javafx.beans.value.*

// Any.

infix fun <E> ObservableObjectValue<E>.transformed(action: (E) -> String): ObservableStringValue {
    return Bindings.createStringBinding({ action(value) }, this)
}

fun <E> ObservableObjectValue<E>.isAnyOf(vararg elements: E): ObservableBooleanValue {
    return Bindings.createBooleanBinding({ elements.contains(value) }, this)
}

inline fun <E> ObservableValue<E>.onZero(crossinline action: () -> Unit) {
    addListener { _, _, _ -> action() }
}

inline fun <E> ObservableValue<E>.onOne(crossinline action: (E?) -> Unit) {
    addListener { _, _, value -> action(value) }
}

inline fun <E> ObservableValue<E>.onTwo(crossinline action: (E?, E?) -> Unit) {
    addListener { _, prev, value -> action(prev, value) }
}

inline fun ObservableDoubleValue.on(crossinline action: (Double) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

inline fun ObservableIntegerValue.on(crossinline action: (Int) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

inline fun ObservableLongValue.on(crossinline action: (Long) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

inline fun ObservableBooleanValue.on(crossinline action: (Boolean) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

inline fun ObservableStringValue.on(crossinline action: (String) -> Unit) {
    addListener { _, _, _ -> action(get()) }
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

fun <E> ObservableBooleanValue.between(onTrue: E?, onFalse: E?): ObservableValue<out E?> {
    return Bindings.createObjectBinding({ if (value) onTrue else onFalse }, this)
}
