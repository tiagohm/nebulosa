package nebulosa.indi.protocol

sealed interface LightVector<E : LightElement> : Vector<E> {

    override val type
        get() = PropertyType.LIGHT
}
