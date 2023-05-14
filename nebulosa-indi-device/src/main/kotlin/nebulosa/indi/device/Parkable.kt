package nebulosa.indi.device

interface Parkable {

    val canPark: Boolean

    val parking: Boolean

    val parked: Boolean

    fun park()

    fun unpark()
}
