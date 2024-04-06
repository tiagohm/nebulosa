package nebulosa.image.format

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/Modifier.kt

sealed interface ImageModifier {

    fun foldIn(operation: (Element) -> Unit)

    fun foldOut(operation: (Element) -> Unit)

    infix fun then(other: ImageModifier): ImageModifier = if (other === ImageModifier) this else Combined(this, other)

    interface Element : ImageModifier {

        override fun foldIn(operation: (Element) -> Unit) = operation(this)

        override fun foldOut(operation: (Element) -> Unit) = operation(this)
    }

    private data class Combined(
        private val outer: ImageModifier,
        private val inner: ImageModifier,
    ) : ImageModifier {

        override fun foldIn(operation: (Element) -> Unit) {
            outer.foldIn(operation)
            inner.foldIn(operation)
        }

        override fun foldOut(operation: (Element) -> Unit) {
            inner.foldOut(operation)
            outer.foldOut(operation)
        }
    }

    companion object : ImageModifier {

        override fun foldIn(operation: (Element) -> Unit) = Unit

        override fun foldOut(operation: (Element) -> Unit) = Unit

        override fun then(other: ImageModifier) = other
    }
}
