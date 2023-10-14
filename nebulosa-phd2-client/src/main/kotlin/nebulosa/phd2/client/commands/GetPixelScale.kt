package nebulosa.phd2.client.commands

data object GetPixelScale : PHD2Command<Double> {

    override val methodName = "get_pixel_scale"

    override val params = null

    override val responseType = Double::class.java
}
