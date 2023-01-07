package nebulosa.indi.protocol

sealed interface MinMaxElement<T : Comparable<T>> : Element<T>, HasMinMax<T>
