package nebulosa.fits

enum class ImageChannel(@JvmField val offset: Int) {
    RED(0),
    GREEN(1),
    BLUE(2),
    GRAY(0);

    companion object {

        @JvmStatic val RGB = listOf(RED, GREEN, BLUE)
    }
}
