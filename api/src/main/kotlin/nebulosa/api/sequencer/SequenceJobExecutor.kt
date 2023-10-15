package nebulosa.api.sequencer

import nebulosa.indi.device.Device

interface SequenceJobExecutor<in T, J : SequenceJob> : Iterable<J> {

    fun execute(data: T): J

    fun sequenceJobFor(vararg devices: Device): J? {
        fun find(task: J): Boolean {
            for (i in devices.indices) {
                if (i >= task.devices.size || task.devices[i].name != devices[i].name) {
                    return false
                }
            }

            return true
        }

        return findLast(::find)
    }

    fun sequenceJobWithId(jobId: Long): J? {
        return find { it.jobId == jobId }
    }
}
