package nebulosa.phd2.client.commands

import nebulosa.guiding.GuideDirection

data class GuidePulse(
    val amount: Int, // ms or step count
    val direction: GuideDirection,
    val which: WhichMount = WhichMount.MOUNT,
) : PHD2Command<Int> {

    override val methodName = "guide_pulse"

    override val params = listOf(amount, direction, which)

    override val responseType = Int::class.java
}
