package nebulosa.indi.protocol

sealed interface SetVector<E : OneElement<*>> : INDIProtocol, Vector<E> {

    var timeout: Double
}
