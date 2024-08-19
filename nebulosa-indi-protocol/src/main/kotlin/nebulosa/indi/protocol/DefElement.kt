package nebulosa.indi.protocol

sealed interface DefElement<T> : Element<T> {

    var label: String
}
