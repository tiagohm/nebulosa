package nebulosa.fits

import java.awt.image.DataBuffer

class FitsDataBuffer(size: Int) : DataBuffer(TYPE_FLOAT, size) {

    val data = FloatArray(size)

    override fun getElem(i: Int): Int {
        return (data[i + offset] * 255f).toInt()
    }

    override fun getElem(bank: Int, i: Int): Int {
        return (data[i + offsets[bank]] * 255f).toInt()
    }

    override fun setElem(i: Int, value: Int) {
        data[i + offset] = value / 255f
    }

    override fun setElem(bank: Int, i: Int, value: Int) {
        data[i + offsets[bank]] = value / 255f
    }

    override fun getElemFloat(i: Int): Float {
        return data[i + offset]
    }

    override fun getElemFloat(bank: Int, i: Int): Float {
        return data[i + offsets[bank]] * 255f
    }

    override fun setElemFloat(i: Int, value: Float) {
        data[i + offset] = value
    }

    override fun setElemFloat(bank: Int, i: Int, value: Float) {
        data[i + offsets[bank]] = value
    }

    override fun getElemDouble(i: Int): Double {
        return data[i + offset].toDouble()
    }

    override fun getElemDouble(bank: Int, i: Int): Double {
        return (data[i + offsets[bank]] * 255f).toDouble()
    }

    override fun setElemDouble(i: Int, value: Double) {
        data[i + offset] = value.toFloat()
    }

    override fun setElemDouble(bank: Int, i: Int, value: Double) {
        data[i + offsets[bank]] = value.toFloat()
    }
}
