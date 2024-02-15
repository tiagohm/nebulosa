package nebulosa.fits

interface WritableHeader {

    fun clear()

    fun add(key: FitsHeader, value: Boolean): HeaderCard

    fun add(key: FitsHeader, value: Int): HeaderCard

    fun add(key: FitsHeader, value: Double): HeaderCard

    fun add(key: FitsHeader, value: String): HeaderCard

    fun add(key: String, value: Boolean, comment: String = ""): HeaderCard

    fun add(key: String, value: Int, comment: String = ""): HeaderCard

    fun add(key: String, value: Double, comment: String = ""): HeaderCard

    fun add(key: String, value: String, comment: String = ""): HeaderCard

    fun add(card: HeaderCard)

    fun delete(key: FitsHeader): Boolean
}
