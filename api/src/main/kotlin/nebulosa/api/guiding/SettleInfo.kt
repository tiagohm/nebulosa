package nebulosa.api.guiding

import nebulosa.guiding.Guider
import org.hibernate.validator.constraints.Range

data class SettleInfo(
    @Range(min = 1, max = 25) @JvmField val amount: Double = 1.5,
    @Range(min = 1, max = 60) @JvmField val time: Long = 10,
    @Range(min = 1, max = 60) @JvmField val timeout: Long = 30,
) {

    companion object {

        @JvmStatic val EMPTY = SettleInfo()

        @JvmStatic
        fun from(guider: Guider) =
            SettleInfo(guider.settleAmount, guider.settleTime.toSeconds(), guider.settleTimeout.toSeconds())
    }
}
