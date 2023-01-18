package nebulosa.indi.device

sealed interface Property<T> {

    val name: String

    val label: String

    val value: T
}
