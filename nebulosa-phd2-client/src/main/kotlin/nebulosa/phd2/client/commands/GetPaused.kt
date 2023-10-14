package nebulosa.phd2.client.commands

data object GetPaused : PHD2Command<Boolean> {

    override val methodName = "get_paused"

    override val params = null

    override val responseType = Boolean::class.java
}
