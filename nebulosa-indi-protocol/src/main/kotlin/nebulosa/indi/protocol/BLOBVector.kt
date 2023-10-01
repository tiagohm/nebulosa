package nebulosa.indi.protocol

sealed interface BLOBVector<E : BLOBElement> : Vector<E> {

    override val type
        get() = PropertyType.BLOB
}
