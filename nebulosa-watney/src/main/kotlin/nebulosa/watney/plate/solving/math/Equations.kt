package nebulosa.watney.plate.solving.math

/**
 * Solving least squares, for solving the plate constants.
 */
@Suppress("LocalVariableName")
fun solveLeastSquares(equationsOfCondition: List<DoubleArray>): DoubleArray {
    // See: https://phys.libretexts.org/Bookshelves/Astronomy__Cosmology/Book%3A_Celestial_Mechanics_(Tatum)/01%3A_Numerical_Methods/1.08%3A_Simultaneous_Linear_Equations_N__n
    val A11 = equationsOfCondition.sumOf { it[0] * it[0] }
    val A12 = equationsOfCondition.sumOf { it[0] * it[1] }
    val A13 = equationsOfCondition.sumOf { it[0] * it[2] }
    val B1 = equationsOfCondition.sumOf { it[0] * it[3] }
    val A22 = equationsOfCondition.sumOf { it[1] * it[1] }
    val A23 = equationsOfCondition.sumOf { it[1] * it[2] }
    val B2 = equationsOfCondition.sumOf { it[1] * it[3] }
    val A33 = equationsOfCondition.sumOf { it[2] * it[2] }
    val B3 = equationsOfCondition.sumOf { it[2] * it[3] }

    // See: https://www.cliffsnotes.com/study-guides/algebra/algebra-ii/linear-equations-in-three-variables/linear-equations-solutions-using-determinants-with-three-variables
    val denominator = A11 * (A22 * A33 - A23 * A23) - A12 * (A12 * A33 - A13 * A23) + A13 * (A12 * A23 - A13 * A22)
    val dx = (-B1) * (A22 * A33 - A23 * A23) - (-B2) * (A12 * A33 - A13 * A23) + (-B3) * (A12 * A23 - A13 * A22)
    val dy = A11 * ((-B2) * A33 - A23 * (-B3)) - A12 * ((-B1) * A33 - A13 * (-B3)) + A13 * ((-B1) * A23 - A13 * (-B2))
    val dz = A11 * (A22 * (-B3) - (-B2) * A23) - A12 * (A12 * (-B3) - (-B1) * A23) + A13 * (A12 * (-B2) - (-B1) * A22)

    val x1 = dx / denominator
    val x2 = dy / denominator
    val x3 = dz / denominator

    return doubleArrayOf(x1, x2, x3)
}

fun lerp(v0: Double, v1: Double, t: Double) = (1 - t) * v0 + t * v1
