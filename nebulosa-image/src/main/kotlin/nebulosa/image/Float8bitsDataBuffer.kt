package nebulosa.image

import java.awt.image.DataBuffer

@Suppress("ArrayInDataClass")
data class Float8bitsDataBuffer(
    @JvmField val mono: Boolean,
    @JvmField val r: FloatArray, // or gray.
    @JvmField val g: FloatArray = r,
    @JvmField val b: FloatArray = r,
) : DataBuffer(TYPE_FLOAT, if (mono) r.size else r.size + g.size + b.size) {

    @JvmField val data = arrayOf(r, g, b)

    @Suppress("NOTHING_TO_INLINE")
    private inline operator fun get(i: Int): Float {
        return data[if (mono) 0 else i % 3][if (mono) i else i / 3]
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline operator fun set(i: Int, value: Float) {
        data[if (mono) 0 else i % 3][if (mono) i else i / 3] = value
    }

    override fun getElem(i: Int): Int {
        return (this[i + offset] * 255f).toInt()
    }

    override fun getElem(bank: Int, i: Int): Int {
        return (this[i + offsets[bank]] * 255f).toInt()
    }

    override fun setElem(i: Int, value: Int) {
        this[i + offset] = value / 255f
    }

    override fun setElem(bank: Int, i: Int, value: Int) {
        this[i + offsets[bank]] = value / 255f
    }

    override fun getElemFloat(i: Int): Float {
        return this[i + offset]
    }

    override fun getElemFloat(bank: Int, i: Int): Float {
        return this[i + offsets[bank]]
    }

    override fun setElemFloat(i: Int, value: Float) {
        this[i + offset] = value
    }

    override fun setElemFloat(bank: Int, i: Int, value: Float) {
        this[i + offsets[bank]] = value
    }

    override fun getElemDouble(i: Int): Double {
        return this[i + offset].toDouble()
    }

    override fun getElemDouble(bank: Int, i: Int): Double {
        return this[i + offsets[bank]].toDouble()
    }

    override fun setElemDouble(i: Int, value: Double) {
        this[i + offset] = value.toFloat()
    }

    override fun setElemDouble(bank: Int, i: Int, value: Double) {
        this[i + offsets[bank]] = value.toFloat()
    }

    companion object {

        @JvmStatic
        fun mono(size: Int) = Float8bitsDataBuffer(true, FloatArray(size))

        @JvmStatic
        fun rgb(size: Int) = Float8bitsDataBuffer(false, FloatArray(size), FloatArray(size), FloatArray(size))
    }
}
