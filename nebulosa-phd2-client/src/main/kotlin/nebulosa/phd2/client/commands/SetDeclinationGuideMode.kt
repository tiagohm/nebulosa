package nebulosa.phd2.client.commands

data class SetDeclinationGuideMode(val mode: DeclinationGuideMode) : PHD2Command<Int> {

    override val methodName = "set_dec_guide_mode"

    override val params = listOf(mode)

    override val responseType = Int::class.java
}
