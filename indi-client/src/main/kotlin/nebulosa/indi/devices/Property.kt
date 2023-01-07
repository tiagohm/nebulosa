package nebulosa.indi.devices

sealed interface Property<T> {

    val name: String

    val label: String

    val value: T
}
