package nebulosa.api.guiding

import nebulosa.api.validators.Validatable
import nebulosa.api.validators.range

data class SettleInfo(
    @JvmField val amount: Double = 1.5,
    @JvmField val time: Long = 10,
    @JvmField val timeout: Long = 30,
) : Validatable {

    override fun validate() {
        amount.range(1.0, 25.0)
        time.range(1L, 60L)
        timeout.range(1L, 60L)
    }

    companion object {

        @JvmStatic val EMPTY = SettleInfo()
    }
}
