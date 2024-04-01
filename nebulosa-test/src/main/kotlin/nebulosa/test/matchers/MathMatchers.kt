@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.test.matchers

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.doubles.ToleranceMatcher
import nebulosa.math.Matrix3D
import nebulosa.math.Vector3D

inline infix fun Vector3D.plusOrMinus(tolerance: Double) = Vector3DMatcher(this, tolerance)

inline infix fun Matrix3D.plusOrMinus(tolerance: Double) = Matrix3DMatcher(this, tolerance)

class Vector3DMatcher(expected: Vector3D, tolerance: Double) : Matcher<Vector3D> {

    private val xMatcher = ToleranceMatcher(expected.x, tolerance)
    private val yMatcher = ToleranceMatcher(expected.y, tolerance)
    private val zMatcher = ToleranceMatcher(expected.z, tolerance)

    override fun test(value: Vector3D): MatcherResult {
        return xMatcher.test(value.x).takeIf { !it.passed() }
            ?: yMatcher.test(value.y).takeIf { !it.passed() }
            ?: zMatcher.test(value.z)
    }
}

class Matrix3DMatcher(expected: Matrix3D, tolerance: Double) : Matcher<Matrix3D> {

    private val a11Matcher = ToleranceMatcher(expected.a11, tolerance)
    private val a12Matcher = ToleranceMatcher(expected.a12, tolerance)
    private val a13Matcher = ToleranceMatcher(expected.a13, tolerance)
    private val a21Matcher = ToleranceMatcher(expected.a21, tolerance)
    private val a22Matcher = ToleranceMatcher(expected.a22, tolerance)
    private val a23Matcher = ToleranceMatcher(expected.a23, tolerance)
    private val a31Matcher = ToleranceMatcher(expected.a31, tolerance)
    private val a32Matcher = ToleranceMatcher(expected.a32, tolerance)
    private val a33Matcher = ToleranceMatcher(expected.a33, tolerance)

    override fun test(value: Matrix3D): MatcherResult {
        return a11Matcher.test(value.a11).takeIf { !it.passed() }
            ?: a12Matcher.test(value.a12).takeIf { !it.passed() }
            ?: a13Matcher.test(value.a13).takeIf { !it.passed() }
            ?: a21Matcher.test(value.a21).takeIf { !it.passed() }
            ?: a22Matcher.test(value.a22).takeIf { !it.passed() }
            ?: a23Matcher.test(value.a23).takeIf { !it.passed() }
            ?: a31Matcher.test(value.a31).takeIf { !it.passed() }
            ?: a32Matcher.test(value.a32).takeIf { !it.passed() }
            ?: a33Matcher.test(value.a33)
    }
}
