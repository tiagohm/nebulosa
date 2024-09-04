package nebulosa.api.alignment.polar.tppa

import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment
import nebulosa.job.manager.Task

data class TPPAAlignmentTask(
    @JvmField val job: TPPAJob,
    @JvmField val alignment: ThreePointPolarAlignment,
) : Task {

    private val mount = job.mount
    private val request = job.request

    override fun run() {
        val radius = ATTEMPT_RADIUS * (job.noSolutionAttempts + 1)

        val result = alignment.align(
            job.savedPath!!, mount.rightAscension, mount.declination, radius,
            request.compensateRefraction // TODO: CANCELLATION TOKEN?
        )

        job.accept(result)
    }

    companion object {

        private const val ATTEMPT_RADIUS = ThreePointPolarAlignment.DEFAULT_RADIUS / 2.0
    }
}
