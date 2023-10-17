package nebulosa.phd2.client.commands

data class FindStar(
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
) : PHD2Command<IntArray> {

    override val methodName = "find_star"

    override val params = mapOf("roi" to if (width > 0 && height > 0) listOf(x, y, width, height) else null)

    override val responseType = IntArray::class.java
}
