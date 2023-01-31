package nebulosa.desktop.logic.util

fun <T> MutableList<T>.toggle(ifTrue: T, ifFalse: T, state: Boolean) {
    if (state) {
        if (ifFalse in this) remove(ifFalse)
        if (ifTrue !in this) add(ifTrue)
    } else {
        if (ifTrue in this) remove(ifTrue)
        if (ifFalse !in this) add(ifFalse)
    }
}
