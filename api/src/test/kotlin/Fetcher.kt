import nebulosa.simbad.SimbadService

sealed interface Fetcher<out T> {

    fun fetch(service: SimbadService): T
}
