package nebulosa.api.mounts

import nebulosa.indi.device.mount.MountEvent

fun interface MountEventAware {

    fun handleMountEvent(event: MountEvent)
}
