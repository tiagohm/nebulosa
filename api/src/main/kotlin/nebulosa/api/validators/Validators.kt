@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.validators

import nebulosa.time.SystemClock
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.exists

// https://github.com/hibernate/hibernate-validator/blob/main/engine/src/main/resources/org/hibernate/validator/ValidationMessages.properties

inline fun <T> T.validate(value: Boolean, lazyMessage: () -> String) = apply { require(value, lazyMessage) }

// ANY

inline fun <T> T?.notNull(message: String? = null) = requireNotNull(this) { message ?: "must not be null" }

// TEXT

inline fun String.notEmpty(message: String? = null) = validate(isNotEmpty()) { message ?: "must not be empty" }
inline fun String.notBlank(message: String? = null) = validate(isNotBlank()) { message ?: "must not be blank" }
inline fun String?.notNullOrEmpty(message: String? = null) = if (isNullOrEmpty()) throw IllegalArgumentException(message ?: "must not be empty") else this
inline fun String?.notNullOrBlank(message: String? = null) = if (isNullOrBlank()) throw IllegalArgumentException(message ?: "must not be blank") else this
inline fun String.minLength(min: Int, message: String? = null) = validate(length >= min) { message ?: "length must be greater than or equal to $min" }
inline fun String.maxLength(max: Int, message: String? = null) = validate(length <= max) { message ?: "length must be greater than or equal to $max" }
inline fun String.regex(pattern: Regex, message: String? = null) = validate(pattern.matches(this)) { message ?: "must match \"$pattern\"" }

// INT

inline fun Int.min(min: Int, message: String? = null) = validate(this >= min) { message ?: "must be greater than or equal to $min" }
inline fun Int.max(max: Int, message: String? = null) = validate(this <= max) { message ?: "must be less than or equal to $max" }
inline fun Int.range(min: Int, max: Int, message: String? = null) = validate(this in min..max) { message ?: "must be between $min and $max" }
inline fun Int.positive(message: String? = null) = validate(this > 0) { message ?: "must be greater than 0" }
inline fun Int.positiveOrZero(message: String? = null) = validate(this >= 0) { message ?: "must be greater than or equal to 0" }

// LONG

inline fun Long.min(min: Long, message: String? = null) = validate(this >= min) { message ?: "must be greater than or equal to $min" }
inline fun Long.max(max: Long, message: String? = null) = validate(this <= max) { message ?: "must be less than or equal to $max" }
inline fun Long.range(min: Long, max: Long, message: String? = null) = validate(this in min..max) { message ?: "must be between $min and $max" }
inline fun Long.positive(message: String? = null) = validate(this > 0) { message ?: "must be greater than 0" }
inline fun Long.positiveOrZero(message: String? = null) = validate(this >= 0) { message ?: "must be greater than or equal to 0" }

// FLOAT

inline fun Float.min(min: Float, message: String? = null) = validate(this >= min) { message ?: "must be greater than or equal to $min" }
inline fun Float.max(max: Float, message: String? = null) = validate(this <= max) { message ?: "must be less than or equal to $max" }
inline fun Float.range(min: Float, max: Float, message: String? = null) = validate(this in min..max) { message ?: "must be between $min and $max" }
inline fun Float.positive(message: String? = null) = validate(this > 0) { message ?: "must be greater than 0" }
inline fun Float.positiveOrZero(message: String? = null) = validate(this >= 0) { message ?: "must be greater than or equal to 0" }

// DOUBLE

inline fun Double.min(min: Double, message: String? = null) = validate(this >= min) { message ?: "must be greater than or equal to $min" }
inline fun Double.max(max: Double, message: String? = null) = validate(this <= max) { message ?: "must be less than or equal to $max" }
inline fun Double.range(min: Double, max: Double, message: String? = null) = validate(this in min..max) { message ?: "must be between $min and $max" }
inline fun Double.positive(message: String? = null) = validate(this > 0) { message ?: "must be greater than 0" }
inline fun Double.positiveOrZero(message: String? = null) = validate(this >= 0) { message ?: "must be greater than or equal to 0" }

// PATH

inline fun String.path() = Path(this)
inline fun Path.notBlank(message: String? = null) = validate("$this".isNotBlank()) { message ?: "path must not be blank" }
inline fun Path.exists(message: String? = null) = validate(exists()) { message ?: "path must exist" }

// DURATION

inline fun Duration.positive(message: String? = null) = validate(!isNegative && !isZero) { message ?: "must be greater than 0" }
inline fun Duration.positiveOrZero(message: String? = null) = validate(!isNegative) { message ?: "must be greater than or equal to 0" }
inline fun Duration.min(duration: Long, unit: TimeUnit, message: String? = null) = validate(toNanos() >= unit.toNanos(duration)) { message ?: "must be greater than or equal to $duration $unit" }
inline fun Duration.min(duration: Duration, message: String? = null) = validate(this >= duration) { message ?: "must be greater than or equal to $duration" }
inline fun Duration.max(duration: Long, unit: TimeUnit, message: String? = null) = validate(toNanos() <= unit.toNanos(duration)) { message ?: "must be less than or equal to $duration $unit" }
inline fun Duration.max(duration: Duration, message: String? = null) = validate(this <= duration) { message ?: "must be less than or equal to $duration" }
inline fun Duration.range(min: Duration, max: Duration, message: String? = null) = validate(this in min..max) { message ?: "must be between $min and $max" }

// COLLECTION

inline fun <T> Collection<T>.notEmpty(message: String? = null) = validate(isNotEmpty()) { message ?: "must not be empty" }
inline fun <T> Collection<T>.minSize(min: Int, message: String? = null) = validate(size >= min) { message ?: "size must be greater than or equal to $min" }

// DATE & TIME

inline fun String.localDate(): LocalDate = LocalDate.parse(this)
inline fun LocalDate.future(message: String? = null) = validate(this > LocalDate.now(SystemClock)) { message ?: "must be a future date" }
inline fun LocalDate.futureOrPresent(message: String? = null) = validate(this >= LocalDate.now(SystemClock)) { message ?: "must be a date in the present or in the future" }
inline fun LocalDate.past(message: String? = null) = validate(this < LocalDate.now(SystemClock)) { message ?: "must be a past date" }
inline fun LocalDate.pastOrPresent(message: String? = null) = validate(this <= LocalDate.now(SystemClock)) { message ?: "must be a date in the past or in the present" }

inline fun String.localTime(): LocalTime = LocalTime.parse(this)
inline fun LocalTime.future(message: String? = null) = validate(this > LocalTime.now(SystemClock)) { message ?: "must be a future time" }
inline fun LocalTime.futureOrPresent(message: String? = null) = validate(this >= LocalTime.now(SystemClock)) { message ?: "must be a time in the present or in the future" }
inline fun LocalTime.past(message: String? = null) = validate(this < LocalTime.now(SystemClock)) { message ?: "must be a past date" }
inline fun LocalTime.pastOrPresent(message: String? = null) = validate(this <= LocalTime.now(SystemClock)) { message ?: "must be a time in the past or in the present" }

// ENUM

inline fun <reified T : Enum<T>> String.enumOf() = enumValueOf<T>(this)

// BODY

inline fun <T : Validatable> T.valid() = apply(Validatable::validate)
