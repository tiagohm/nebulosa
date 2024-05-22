package nebulosa.astrobin.api

sealed interface Equipment {

    val id: Long

    val brandName: String

    val name: String

    val isValid: Boolean
}
