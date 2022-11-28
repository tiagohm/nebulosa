package nebulosa.io

interface Seekable : Exhaustible, Skippable {

    val position: Long

    fun seek(position: Long)

    override fun skip(byteCount: Long) = seek(position + byteCount)
}
