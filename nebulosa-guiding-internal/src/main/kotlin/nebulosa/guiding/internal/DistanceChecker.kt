package nebulosa.guiding.internal

import nebulosa.log.loggerFor

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
            LOG.info("activated")
            state = State.WAITING
            expires = System.currentTimeMillis() + WAIT_INTERVAL
            forceTolerance = 2.0
        }
    }

    private fun checkIfSmallOffset(distance: Double, raOnly: Boolean, tolerance: Double): Boolean {
        if (!guider.isGuiding || guider.isPaused || guider.settling || guider.currentErrorFrameCount < 10) {
            return true
        }

        val avgDist = guider.currentErrorSmoothed(raOnly)
        val threshold = tolerance * avgDist

        return if (distance > threshold) {
            LOG.info(
                "reject for large offset. distance={}, threshold={} avgDist={}, count={}",
                distance, threshold, avgDist, guider.currentErrorFrameCount,
            )

            false
        } else {
            true
        }
    }

    fun checkDistance(distance: Double, raOnly: Boolean, tolerance: Double): Boolean {
        val smallOffset = checkIfSmallOffset(distance, raOnly, if (forceTolerance != 0.0) forceTolerance else tolerance)

        return when (state) {
            State.GUIDING -> {
                if (smallOffset) {
                    true
                } else {
                    LOG.info("activated")
                    state = State.WAITING
                    expires = System.currentTimeMillis() + WAIT_INTERVAL
                    false
                }
            }

            State.WAITING -> {
                if (smallOffset) {
                    LOG.info("deactivated")
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
                        LOG.info("begin recovering")
                        true
                    }
                }
            }

            State.RECOVERING -> {
                if (smallOffset) {
                    LOG.info("deactivated")
                    state = State.GUIDING
                }

                true
            }
        }
    }

    companion object {

        private const val WAIT_INTERVAL = 5000L

        @JvmStatic
        private val LOG = loggerFor<DistanceChecker>()
    }
}
