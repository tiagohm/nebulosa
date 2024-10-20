package nebulosa.api.alignment.polar.tppa

import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment
import nebulosa.job.manager.Task

data class TPPATask(
    @JvmField val job: TPPAJob,
    @JvmField val alignment: ThreePointPolarAlignment,
) : Task {

    override fun run() {
        val mount = job.mount
        val radius = ATTEMPT_RADIUS * (job.noSolutionAttempts + 1)
        val result = alignment.align(job.savedPath!!, mount.rightAscension, mount.declination, radius)
        job.accept(result)
    }

    companion object {

        private const val ATTEMPT_RADIUS = ThreePointPolarAlignment.DEFAULT_RADIUS / 2.0
    }
}
