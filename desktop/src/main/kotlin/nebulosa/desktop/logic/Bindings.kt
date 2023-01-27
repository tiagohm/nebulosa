@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.desktop.logic

import com.sun.javafx.binding.StringFormatter
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableObjectValue
import javafx.beans.value.ObservableValue
import java.util.*

inline fun ObservableObjectValue<*>.isEqualTo(other: Any): BooleanBinding {
    return Bindings.equal(this, other)
}

inline fun ObservableObjectValue<*>.isEqualTo(other: ObservableObjectValue<*>): BooleanBinding {
    return Bindings.equal(this, other)
}

inline fun ObservableObjectValue<*>.isNotEqualTo(other: Any): BooleanBinding {
    return Bindings.notEqual(this, other)
}

inline fun ObservableObjectValue<*>.isNotEqualTo(other: ObservableObjectValue<*>): BooleanBinding {
    return Bindings.notEqual(this, other)
}

inline val ObservableObjectValue<*>.isNull: BooleanBinding
    get() = Bindings.isNull(this)

inline val ObservableObjectValue<*>.isNotNull: BooleanBinding
    get() = Bindings.isNotNull(this)

inline fun ObservableValue<*>.asString(): StringBinding {
    return StringFormatter.convert(this) as StringBinding
}

inline fun ObservableValue<*>.asString(format: String): StringBinding {
    return Bindings.format(format, this) as StringBinding
}

inline fun ObservableValue<*>.asString(locale: Locale, format: String): StringBinding {
    return Bindings.format(locale, format, this) as StringBinding
}
