package nebulosa.indi.protocol

interface Element<out T> : HasName {

    val value: T
}
