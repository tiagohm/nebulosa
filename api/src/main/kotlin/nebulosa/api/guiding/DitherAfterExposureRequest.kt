package nebulosa.api.guiding

import jakarta.validation.constraints.Positive

data class DitherAfterExposureRequest(
    val enabled: Boolean = true,
    @field:Positive val amount: Double = 1.5,
    val raOnly: Boolean = false,
    @field:Positive val afterExposures: Int = 1,
) {

    companion object {

        @JvmStatic val DISABLED = DitherAfterExposureRequest(false)
    }
}
