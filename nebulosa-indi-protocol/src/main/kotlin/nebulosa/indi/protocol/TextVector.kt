package nebulosa.indi.protocol

sealed interface TextVector<E : TextElement> : Vector<E> {

    override val type
        get() = PropertyType.TEXT
}
