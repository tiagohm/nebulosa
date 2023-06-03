package nebulosa.desktop.gui.control

sealed interface ComboBoxItem<out T> {

    val item: T?

    data class Valued<out T>(override val item: T) : ComboBoxItem<T>

    class Null<out T> : ComboBoxItem<T> {

        override val item: T?
            get() = null
    }
}
