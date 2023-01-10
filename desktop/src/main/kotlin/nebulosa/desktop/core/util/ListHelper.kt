package nebulosa.desktop.core.util

fun <T> MutableList<T>.toggle(toAdd: T, toRemove: T, state: Boolean = true) {
    val a = if (state) toAdd else toRemove
    val b = if (state) toRemove else toAdd
    val i = indexOf(a)

    if (i < 0) {
        val k = indexOf(b)
        if (k >= 0) set(k, a)
        else add(a)
    } else {
        set(i, b)
    }
}
