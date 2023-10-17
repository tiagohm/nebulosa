package nebulosa.phd2.client.commands

data object GetDeclinationGuideMode : PHD2Command<DeclinationGuideMode> {

    override val methodName = "get_dec_guide_mode"

    override val params = null

    override val responseType = DeclinationGuideMode::class.java
}
