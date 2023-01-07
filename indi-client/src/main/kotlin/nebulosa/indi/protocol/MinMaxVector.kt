package nebulosa.indi.protocol

sealed interface MinMaxVector<E> : Vector<E> where E : MinMaxElement<*>
