package nebulosa.phd2.client.commands

data object GetConnected : PHD2Command<Boolean> {

    override val methodName = "get_connected"

    override val params = null

    override val responseType = Boolean::class.java
}
