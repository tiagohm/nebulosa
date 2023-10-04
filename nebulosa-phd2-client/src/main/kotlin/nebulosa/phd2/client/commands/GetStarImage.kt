package nebulosa.phd2.client.commands

data class GetStarImage(val size: Int = 15) : PHD2Command<StarImage> {

    override val methodName = "get_star_image"

    override val params = listOf(size)

    override val responseType = StarImage::class.java
}
