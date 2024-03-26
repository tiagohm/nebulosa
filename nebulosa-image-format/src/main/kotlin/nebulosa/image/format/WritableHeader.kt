package nebulosa.image.format

interface WritableHeader {

    fun clear()

    fun add(key: HeaderKey, value: Boolean) = add(key.key, value, key.comment)

    fun add(key: HeaderKey, value: Int) = add(key.key, value, key.comment)

    fun add(key: HeaderKey, value: Double) = add(key.key, value, key.comment)

    fun add(key: HeaderKey, value: String) = add(key.key, value, key.comment)

    fun add(key: String, value: Boolean, comment: String = "")

    fun add(key: String, value: Int, comment: String = "")

    fun add(key: String, value: Double, comment: String = "")

    fun add(key: String, value: String, comment: String = "")

    fun delete(key: HeaderKey) = delete(key.key)

    fun delete(key: String): Boolean
}
