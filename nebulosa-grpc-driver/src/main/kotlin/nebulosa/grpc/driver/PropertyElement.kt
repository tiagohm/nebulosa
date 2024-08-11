package nebulosa.grpc.driver

sealed interface PropertyElement<T> {

    val name: String

    val label: String

    val value: T
}
