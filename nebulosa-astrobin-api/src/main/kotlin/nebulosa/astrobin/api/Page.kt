package nebulosa.astrobin.api

data class Page<T : Equipment>(
    val count: Int = 0,
    val results: List<T> = emptyList(),
    val next: String? = null,
    val previous: String? = null,
)
