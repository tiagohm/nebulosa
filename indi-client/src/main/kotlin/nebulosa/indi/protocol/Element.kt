package nebulosa.indi.protocol

sealed interface Element<out T> : HasName {

    val value: T
}
