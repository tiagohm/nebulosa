package nebulosa.indi.protocol

sealed interface NumberVector<E : NumberElement> : MinMaxVector<E> {

    override val type
        get() = PropertyType.NUMBER
}
