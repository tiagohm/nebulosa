package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.cos
import nebulosa.math.sin
import kotlin.math.acos
import kotlin.math.max

abstract class SkyCatalog<T : SkyObject>(estimatedSize: Int = 0) : Collection<T> {

    private val data = ArrayList<T>(max(32, estimatedSize))

    fun withText(text: String): List<T> {
        return data.filter { it.name.contains(text, true) }
    }

    fun searchAround(rightAscension: Angle, declination: Angle, limitFOV: Angle): List<T> {
        val res = ArrayList<T>(32)
        val cdec = declination.cos
        val sdec = declination.sin

        fun distance(ra: Angle, dec: Angle): Double {
            // FORMULA USANDO DISTANCIA ENTRE COORDENADAS.
            // acos(sin(DEC1)*sin(DEC2) + cos(DEC1)*cos(DEC2)*cos(RA1-RA2))
            return acos(sdec * dec.sin + cdec * dec.cos * (rightAscension - ra).cos)
        }

        // FORMULA USANDO VETORES.
        // val starPos = body.positionAndVelocity.position.normalized
        // val cosAngle = starPos.dot(vector.normalized)
        // val isAround = cosAngle >= limitFOV.cos

        for (entry in data) {
            if (distance(entry.rightAscensionJ2000, entry.declinationJ2000) <= limitFOV) {
                res.add(entry)
            }
        }

        return res
    }

    protected fun notifyLoadFinished() = Unit

    override val size
        get() = data.size

    fun clear() = data.clear()

    protected fun add(element: T) = data.add(element)

    override fun isEmpty() = data.isEmpty()

    override fun containsAll(elements: Collection<T>) = data.containsAll(elements)

    override fun contains(element: T) = element in data

    override fun iterator() = data.iterator()
}
