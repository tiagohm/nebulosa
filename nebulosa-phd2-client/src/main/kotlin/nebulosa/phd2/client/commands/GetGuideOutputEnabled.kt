package nebulosa.phd2.client.commands

data object GetGuideOutputEnabled : PHD2Command<Boolean> {

    override val methodName = "get_guide_output_enabled"

    override val params = null

    override val responseType = Boolean::class.java
}
