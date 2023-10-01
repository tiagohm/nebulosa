package nebulosa.indi.protocol

sealed interface SwitchVector<E : SwitchElement> : Vector<E> {

    override val type
        get() = PropertyType.SWITCH
}
