package nebulosa.indi.protocol

sealed class OneElement<T> : INDIProtocol(), Element<T> {

    override fun toString() = "${this::class.simpleName}(name=$name, message=$message, value=$value)"
}
