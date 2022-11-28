package nebulosa.indi.protocol

interface MinMaxVector<E> : Vector<E> where E : MinMaxElement<*>
