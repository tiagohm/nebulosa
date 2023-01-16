package nebulosa.indi.protocol

interface HasText : CharSequence {

    val text: String

    override val length get() = text.length

    override fun get(index: Int) = text[index]

    override fun subSequence(startIndex: Int, endIndex: Int) = text.subSequence(startIndex, endIndex)
}
