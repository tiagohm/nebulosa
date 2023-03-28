package nebulosa.skycatalog

import nebulosa.erfa.CartesianCoordinate
import nebulosa.erfa.SphericalCoordinate
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.FixedStar

abstract class SkyCatalog<T : SkyObject>(estimatedSize: Int = 0) : Collection<T> {

    private val data = ArrayList<T>(estimatedSize)
    private var positions = HashMap<Int, FixedStar>(0) // TODO: Can be optimized?

    fun position(star: SkyObject): FixedStar {
        return positions[star.id]!!
    }

    fun searchBy(text: String): List<T> {
        return data.stream().filter(SkyCatalogFilter(text)).toList()
    }

    fun searchAround(vector: Vector3D, limitFOV: Angle): List<T> {
        val res = ArrayList<T>()
        val cosLimFov = limitFOV.cos
        val normalizedVector = vector.normalized

        for (star in data) {
            val body = positions[star.id]!!
            val starPos = body.positionAndVelocity.position.normalized
            val cosAngle = starPos.dot(normalizedVector)

            if (cosAngle >= cosLimFov) {
                res.add(star)
            }
        }

        return res
    }

    fun searchAround(coordinate: SphericalCoordinate, limitFOV: Angle): List<T> {
        return searchAround(coordinate.cartesian, limitFOV)
    }

    fun searchAround(coordinate: CartesianCoordinate, limitFOV: Angle): List<T> {
        return searchAround(coordinate.vector, limitFOV)
    }

    fun searchAround(rightAscension: Angle, declination: Angle, limitFOV: Angle): List<T> {
        return searchAround(SphericalCoordinate(rightAscension, declination, Distance.ONE), limitFOV)
    }

    protected fun notifyLoadFinished() {
        positions = HashMap(data.size)

        for (it in data) {
            positions[it.id] = FixedStar(it.rightAscension, it.declination, it.pmRA, it.pmDEC, it.parallax)
        }
    }

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
