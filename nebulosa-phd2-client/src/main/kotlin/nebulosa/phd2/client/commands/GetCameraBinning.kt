package nebulosa.phd2.client.commands

data object GetCameraBinning : PHD2Command<Int> {

    override val methodName = "get_camera_binning"

    override val params = null

    override val responseType = Int::class.java
}
