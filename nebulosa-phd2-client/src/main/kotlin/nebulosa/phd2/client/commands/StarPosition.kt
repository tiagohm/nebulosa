package nebulosa.phd2.client.commands

data class StarPosition(val x: Double = 0.0, val y: Double = 0.0) {

    companion object {

        @JvmStatic val ZERO = StarPosition()
    }
}
