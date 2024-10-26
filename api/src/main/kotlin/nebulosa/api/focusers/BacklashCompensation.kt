package nebulosa.api.focusers

data class BacklashCompensation(
    @JvmField val mode: BacklashCompensationMode = BacklashCompensationMode.OVERSHOOT,
    @JvmField val backlashIn: Int = 0,
    @JvmField val backlashOut: Int = 0,
) {

    companion object {

        val EMPTY = BacklashCompensation()
    }
}
