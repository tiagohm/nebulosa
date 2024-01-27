package nebulosa.math

import kotlin.annotation.AnnotationTarget.*

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@Target(CLASS, ANNOTATION_CLASS, PROPERTY, FIELD, LOCAL_VARIABLE, VALUE_PARAMETER, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS)
@MustBeDocumented
annotation class Unsafe

/**
 * Allows manipulate Vector's array in unsafe mode.
 */
@Unsafe
inline fun <T> Vector3D.unsafe(block: UnsafeVector3D.() -> T) = with(UnsafeVector3D(this), block)

/**
 * Allows manipulate Vector's array in unsafe mode.
 */
@Unsafe
inline fun Vector3D.unsafe(block: UnsafeVector3D.() -> Unit) = UnsafeVector3D(this).also(block).vector

/**
 * Allows manipulate Matrix's array in unsafe mode.
 */
@Unsafe
inline fun <T> Matrix3D.unsafe(block: UnsafeMatrix3D.() -> T) = with(UnsafeMatrix3D(this), block)

/**
 * Allows manipulate Matrix's array in unsafe mode.
 */
@Unsafe
inline fun Matrix3D.unsafe(block: UnsafeMatrix3D.() -> Unit) = UnsafeMatrix3D(this).also(block).matrix

@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class UnsafeVector3D(@JvmField @PublishedApi internal val vector: Vector3D) {

    inline operator fun get(index: Int): Double {
        return vector.vector[index]
    }

    inline operator fun set(index: Int, value: Double) {
        vector.vector[index] = value
    }
}

@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class UnsafeMatrix3D(@JvmField @PublishedApi internal val matrix: Matrix3D) {

    inline operator fun get(index: Int): Double {
        return matrix.matrix[index]
    }

    inline operator fun set(index: Int, value: Double) {
        matrix.matrix[index] = value
    }
}
