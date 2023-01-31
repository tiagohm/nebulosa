@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.desktop.logic

import com.sun.javafx.binding.StringFormatter
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.value.*
import java.util.*

// Any.

inline fun <E> ObservableValue<out E>.on(crossinline action: (E?) -> Unit) {
    addListener { _, _, value -> action(value) }
}

inline fun <E> ObjectProperty<out E>.on(crossinline action: (E?) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

inline infix fun ObservableObjectValue<*>.isEqualTo(other: Any): BooleanBinding {
    return Bindings.equal(this, other)
}

inline infix fun ObservableObjectValue<*>.isEqualTo(other: ObservableObjectValue<*>): BooleanBinding {
    return Bindings.equal(this, other)
}

inline infix fun ObservableObjectValue<*>.isNotEqualTo(other: Any): BooleanBinding {
    return Bindings.notEqual(this, other)
}

inline infix fun ObservableObjectValue<*>.isNotEqualTo(other: ObservableObjectValue<*>): BooleanBinding {
    return Bindings.notEqual(this, other)
}

inline fun ObservableObjectValue<*>.isNull(): BooleanBinding {
    return Bindings.isNull(this)
}

inline fun ObservableObjectValue<*>.isNotNull(): BooleanBinding {
    return Bindings.isNotNull(this)
}

inline fun <E> ObservableObjectValue<out E>.isAnyOf(vararg elements: E): ObservableBooleanValue {
    return Bindings.createBooleanBinding({ elements.contains(get()) }, this)
}

inline fun ObservableValue<*>.asString(): StringBinding {
    return StringFormatter.convert(this) as StringBinding
}

inline fun ObservableValue<*>.asString(format: String): StringBinding {
    return Bindings.format(format, this) as StringBinding
}

inline fun ObservableValue<*>.asString(locale: Locale, format: String): StringBinding {
    return Bindings.format(locale, format, this) as StringBinding
}

inline fun <E> ObservableObjectValue<E>.asString(crossinline action: (E) -> String): ObservableStringValue {
    return Bindings.createStringBinding({ action(get()) }, this)
}

// List.

inline infix fun <E> ObservableListValue<out E>.contains(element: E): BooleanBinding {
    return Bindings.createBooleanBinding({ element in this }, this)
}

inline infix fun <E> ObservableListValue<out E>.notContains(element: E): BooleanBinding {
    return Bindings.createBooleanBinding({ element !in this }, this)
}

// Double.

inline fun ObservableDoubleValue.on(crossinline action: (Double) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

inline fun ObservableDoubleValue.asString(crossinline action: (Double) -> String): ObservableStringValue {
    return Bindings.createStringBinding({ action(get()) }, this)
}

// Int.

inline fun ObservableIntegerValue.on(crossinline action: (Int) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

// Long.

inline fun ObservableLongValue.on(crossinline action: (Long) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

// Boolean.

inline fun ObservableBooleanValue.on(crossinline action: (Boolean) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}

inline infix fun ObservableBooleanValue.and(other: ObservableBooleanValue): BooleanBinding {
    return Bindings.and(this, other)
}

inline infix fun ObservableBooleanValue.or(other: ObservableBooleanValue): BooleanBinding {
    return Bindings.or(this, other)
}

inline infix fun ObservableBooleanValue.xor(other: ObservableBooleanValue): BooleanBinding {
    return Bindings.notEqual(this, other)
}

inline fun <E> ObservableBooleanValue.between(onTrue: E?, onFalse: E?): ObservableValue<out E?> {
    return Bindings.createObjectBinding({ if (get()) onTrue else onFalse }, this)
}

// String.

inline fun ObservableStringValue.on(crossinline action: (String?) -> Unit) {
    addListener { _, _, _ -> action(get()) }
}
