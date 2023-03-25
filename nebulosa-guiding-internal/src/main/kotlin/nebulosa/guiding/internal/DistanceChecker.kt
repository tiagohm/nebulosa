package nebulosa.guiding.internal

internal class DistanceChecker(private val guider: MultiStarGuider) {

    enum class State {
        GUIDING,
        WAITING,
        RECOVERING,
    }

    private var state = State.GUIDING
    private var expires = 0L
    private var forceTolerance = 0.0

    fun activate() {
        if (state == State.GUIDING) {
            state = State.WAITING
            expires = System.currentTimeMillis() + WAIT_INTERVAL
            forceTolerance = 2.0
        }
    }

    private fun internalCheckDistance(distance: Double, raOnly: Boolean, tolerance: Double): Boolean {
        if (!guider.guiding || guider.paused || guider.settling || guider.currentErrorFrameCount < 10) {
            return true
        }

        val avgDist = guider.currentErrorSmoothed(raOnly)
        val threshold = tolerance * avgDist
        return distance <= threshold
    }

    fun checkDistance(distance: Double, raOnly: Boolean, tolerance: Double): Boolean {
        val smallOffset = internalCheckDistance(distance, raOnly, if (forceTolerance != 0.0) forceTolerance else tolerance)

        return when (state) {
            State.GUIDING -> {
                if (smallOffset) {
                    true
                } else {
                    state = State.WAITING
                    expires = System.currentTimeMillis() + WAIT_INTERVAL
                    false
                }
            }
            State.WAITING -> {
                if (smallOffset) {
                    state = State.GUIDING
                    forceTolerance = 0.0
                    true
                } else {
                    val now = System.currentTimeMillis()

                    // Reject frame.
                    if (now < expires) {
                        false
                    } else {
                        // Timed-out.
                        state = State.RECOVERING

                        true
                    }
                }
            }
            State.RECOVERING -> {
                if (smallOffset) {
                    state = State.GUIDING
                }

                true
            }
        }
    }

    companion object {

        private const val WAIT_INTERVAL = 5000L
    }
}
