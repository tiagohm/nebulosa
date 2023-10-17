package nebulosa.phd2.client.commands

data class SetGuideOutputEnabled(val enabled: Boolean) : PHD2Command<Int> {

    override val methodName = "set_guide_output_enabled"

    override val params = listOf(enabled)

    override val responseType = Int::class.java
}
