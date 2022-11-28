package nebulosa.indi.protocol

interface MinMaxElement<T : Comparable<T>> : Element<T>, HasMinMax<T>
