package nebulosa.indi.protocol

sealed interface DefVector<E : DefElement<*>> : INDIProtocol, Vector<E> {

    var group: String

    var label: String

    var perm: PropertyPermission

    var timeout: Double

    companion object {

        inline val DefVector<*>.isReadOnly
            get() = perm == PropertyPermission.RO

        inline val DefVector<*>.isNotReadOnly
            get() = !isReadOnly
    }
}
