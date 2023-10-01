package nebulosa.api.sequencer

import nebulosa.indi.device.Device

interface SequenceJobExecutor<in T> : Iterable<SequenceJob> {

    fun execute(data: T): SequenceJob

    fun sequenceTaskFor(vararg devices: Device): SequenceJob? {
        fun find(task: SequenceJob): Boolean {
            for (i in devices.indices) {
                if (i >= task.devices.size || task.devices[i].name != devices[i].name) {
                    return false
                }
            }

            return true
        }

        return findLast(::find)
    }
}
