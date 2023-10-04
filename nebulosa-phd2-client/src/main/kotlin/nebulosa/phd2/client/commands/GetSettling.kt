package nebulosa.phd2.client.commands

data object GetSettling : PHD2Command<Boolean> {

    override val methodName = "get_settling"

    override val params = null

    override val responseType = Boolean::class.java
}
