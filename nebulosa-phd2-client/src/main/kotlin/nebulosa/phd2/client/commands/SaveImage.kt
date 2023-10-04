package nebulosa.phd2.client.commands

data object SaveImage : PHD2Command<SavedImage> {

    override val methodName = "save_image"

    override val params = null

    override val responseType = SavedImage::class.java
}
