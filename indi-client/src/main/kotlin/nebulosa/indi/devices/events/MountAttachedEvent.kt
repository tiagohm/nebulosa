package nebulosa.indi.devices.events

import nebulosa.indi.devices.mounts.Mount

data class MountAttachedEvent(override val device: Mount) : MountEvent
