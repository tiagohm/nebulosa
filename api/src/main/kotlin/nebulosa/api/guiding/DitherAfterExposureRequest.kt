package nebulosa.api.guiding

import nebulosa.api.javalin.Validatable
import nebulosa.api.javalin.positive

data class DitherAfterExposureRequest(
    @JvmField val enabled: Boolean = true,
    @JvmField val amount: Double = 1.5,
    @JvmField val raOnly: Boolean = false,
    @JvmField val afterExposures: Int = 1,
) : Validatable {

    override fun validate() {
        amount.positive()
        afterExposures.positive()
    }

    companion object {

        @JvmStatic val DISABLED = DitherAfterExposureRequest(false)
    }
}
