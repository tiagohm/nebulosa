package nebulosa.image.format

enum class ImageChannel(@JvmField val index: Int) {
    GRAY(0),
    RED(0),
    GREEN(1),
    BLUE(2);

    companion object {

        @JvmStatic val RGB = listOf(RED, GREEN, BLUE)
    }
}
