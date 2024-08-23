package nebulosa.meeus

object Base {

    /**
     * Evaluates a polynomal with [coefficients] at [time]. The constant term is c[0].
     */
    @JvmStatic
    fun horner(time: Double, vararg coefficients: Double): Double {
        var i = coefficients.size - 1
        var y = coefficients[i]

        while (i-- > 0) {
            y = y * time + coefficients[i]
        }

        return y
    }
}
