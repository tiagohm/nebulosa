package nebulosa.astrobin.api

data class Camera(
    override val id: Long = 0L,
    override val brandName: String = "",
    override val name: String = "",
    val cooled: Boolean = false,
    val sensor: Long = 0L,
) : Equipment
