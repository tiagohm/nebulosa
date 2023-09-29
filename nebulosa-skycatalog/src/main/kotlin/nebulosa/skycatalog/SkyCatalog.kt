package nebulosa.skycatalog

import nebulosa.math.Angle
import kotlin.math.acos

abstract class SkyCatalog<T : SkyObject>(estimatedSize: Int = 0) : Collection<T> {

    private val data = ArrayList<T>(estimatedSize)

    fun searchBy(text: String): List<T> {
        return data.stream().filter(SkyCatalogFilter(text)).toList()
    }

    fun searchAround(rightAscension: Angle, declination: Angle, limitFOV: Angle): List<T> {
        val res = ArrayList<T>(32)
        val deccos = declination.cos
        val decsin = declination.sin

        fun distance(ra: Angle, dec: Angle): Double {
            // FORMULA USANDO DISTANCIA ENTRE COORDENADAS.
            // acos(sin(DEC1)*sin(DEC2) + cos(DEC1)*cos(DEC2)*cos(RA1-RA2))
            return acos(decsin * dec.sin + deccos * dec.cos * (rightAscension - ra).cos)
        }

        // FORMULA USANDO VETORES.
        // val starPos = body.positionAndVelocity.position.normalized
        // val cosAngle = starPos.dot(vector.normalized)
        // val isAround = cosAngle >= limitFOV.cos

        for (it in data) {
            if (distance(it.rightAscensionJ2000, it.declinationJ2000) <= limitFOV.value) {
                res.add(it)
            }
        }

        return res
    }

    protected fun notifyLoadFinished() {}

    override val size
        get() = data.size

    operator fun get(index: Int) = data[index]

    override fun isEmpty() = data.isEmpty()

    protected fun removeFirst() = data.removeFirst()

    protected fun removeLast() = data.removeLast()

    protected fun getFirst() = data.first()

    protected fun getLast() = data.last()

    protected fun add(element: T) = data.add(element)

    protected fun addAll(elements: Collection<T>) = data.addAll(elements)

    protected fun clear() = data.clear()

    override fun iterator() = data.iterator()

    protected fun retainAll(elements: Collection<T>) = data.retainAll(elements.toSet())

    protected fun removeAll(elements: Collection<T>) = data.removeAll(elements.toSet())

    protected fun remove(element: T) = data.remove(element)

    fun lastIndexOf(element: T) = data.lastIndexOf(element)

    fun indexOf(element: T) = data.indexOf(element)

    override fun containsAll(elements: Collection<T>) = data.containsAll(elements)

    override fun contains(element: T) = element in data
}
