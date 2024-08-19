package nebulosa.indi.protocol

sealed interface Element<out T> : HasName, XMLOutput {

    val value: T
}
