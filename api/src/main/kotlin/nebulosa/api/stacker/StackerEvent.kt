package nebulosa.api.stacker

import nebulosa.api.message.MessageEvent

data class StackerEvent(
    @JvmField val state: StackerState = StackerState.IDLE,
    @JvmField val type: StackerGroupType = StackerGroupType.MONO,
    @JvmField val stackCount: Int = 0,
    @JvmField val numberOfTargets: Int = 0,
) : MessageEvent {

    override val eventName = "STACKER.ELAPSED"

    companion object {

        @JvmStatic val IDLE = StackerEvent()
    }
}
