package nebulosa.phd2.client.commands

data object GetExposureDurations : PHD2Command<LongArray> {

    override val methodName = "get_exposure_durations"

    override val params = null

    override val responseType = LongArray::class.java
}
