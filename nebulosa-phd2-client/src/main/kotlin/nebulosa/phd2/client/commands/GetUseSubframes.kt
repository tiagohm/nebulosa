package nebulosa.phd2.client.commands

data object GetUseSubframes : PHD2Command<Boolean> {

    override val methodName = "get_use_subframes"

    override val params = null

    override val responseType = Boolean::class.java
}
