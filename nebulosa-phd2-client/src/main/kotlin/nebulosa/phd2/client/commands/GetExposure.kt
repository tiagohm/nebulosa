package nebulosa.phd2.client.commands

data object GetExposure : PHD2Command<Long> {

    override val methodName = "get_exposure"

    override val params = null

    override val responseType = Long::class.java
}
