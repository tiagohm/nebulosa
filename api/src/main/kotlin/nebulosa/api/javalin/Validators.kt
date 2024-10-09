@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.exists

// https://github.com/hibernate/hibernate-validator/blob/main/engine/src/main/resources/org/hibernate/validator/ValidationMessages.properties

inline fun <T> T.validate(value: Boolean, lazyMessage: () -> String) = apply { require(value, lazyMessage) }

// ANY

inline fun <T> T?.notNull() = requireNotNull(this) { "must not be null" }

// TEXT

inline fun String.notEmpty() = validate(isNotEmpty()) { "must not be empty" }
inline fun String.notBlank() = validate(isNotBlank()) { "must not be blank" }
inline fun String.minLength(min: Int) = validate(length >= min) { "length must be greater than or equal to $min" }
inline fun String.maxLength(max: Int) = validate(length <= max) { "length must be greater than or equal to $max" }
inline fun String.regex(pattern: Regex) = validate(pattern.matches(this)) { "must match \"$pattern\"" }

// INT

inline fun Int.min(min: Int) = validate(this >= min) { "must be greater than or equal to $min" }
inline fun Int.max(max: Int) = validate(this <= max) { "must be less than or equal to $max" }
inline fun Int.range(min: Int, max: Int) = validate(this in min..max) { " must be between $min and $max" }
inline fun Int.positive() = validate(this > 0) { "must be greater than 0" }
inline fun Int.positiveOrZero() = validate(this >= 0) { "must be greater than or equal to 0" }

// LONG

inline fun Long.min(min: Long) = validate(this >= min) { "must be greater than or equal to $min" }
inline fun Long.max(max: Long) = validate(this <= max) { "must be less than or equal to $max" }
inline fun Long.range(min: Long, max: Long) = validate(this in min..max) { " must be between $min and $max" }
inline fun Long.positive() = validate(this > 0) { "must be greater than 0" }
inline fun Long.positiveOrZero() = validate(this >= 0) { "must be greater than or equal to 0" }

// DOUBLE

inline fun Double.min(min: Double) = validate(this >= min) { "must be greater than or equal to $min" }
inline fun Double.max(max: Double) = validate(this <= max) { "must be less than or equal to $max" }
inline fun Double.range(min: Double, max: Double) = validate(this in min..max) { " must be between $min and $max" }
inline fun Double.positive() = validate(this > 0) { "must be greater than 0" }
inline fun Double.positiveOrZero() = validate(this >= 0) { "must be greater than or equal to 0" }

// PATH

inline fun String.path() = Path(this)
inline fun Path.exists() = validate(exists()) { "must exist" }

// DURATION

inline fun Duration.positive() = validate(!isNegative && !isZero) { "must be greater than 0" }
inline fun Duration.positiveOrZero() = validate(!isNegative) { "must be greater than or equal to 0" }

inline fun Duration.min(duration: Long, unit: TimeUnit) =
    validate(toNanos() >= unit.toNanos(duration)) { "must be greater than or equal to $duration $unit" }

inline fun Duration.max(duration: Long, unit: TimeUnit) =
    validate(toNanos() <= unit.toNanos(duration)) { "must be less than or equal to $duration $unit" }

// COLLECTION

inline fun <T> Collection<T>.notEmpty() = validate(isNotEmpty()) { "must not be empty" }
inline fun <T> Collection<T>.minSize(min: Int) = validate(size >= min) { "size must be greater than or equal to $min" }

// BODY

fun interface Validatable {

    fun validate()
}

inline fun <T : Validatable> T.valid() = apply { validate() }
