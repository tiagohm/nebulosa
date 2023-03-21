package nebulosa.skycatalog

import nebulosa.erfa.CartesianCoordinate
import nebulosa.erfa.SphericalCoordinate
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Vector3D
import java.util.*

abstract class SkyCatalog<T : SkyObject> : Collection<T> {

    private val data = LinkedList<T>()

    fun searchBy(text: String): List<T> {
        return data.stream().filter(SkyCatalogFilter(text)).toList()
    }

    fun searchAround(vector: Vector3D, limitFOV: Angle): List<T> {
        val res = ArrayList<T>()
        val cosLimFov = limitFOV.cos
        val normalizedVector = vector.normalized

        for (star in this) {
            val starPos = star.position.positionAndVelocity.position.normalized

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

    override val size
        get() = data.size

    operator fun get(index: Int) = data[index]

    override fun isEmpty() = data.isEmpty()

    protected fun poll(): T? = data.poll()

    protected fun element(): T = data.element()

    protected fun peek(): T? = data.peek()

    protected fun removeFirst(): T = data.removeFirst()

    protected fun removeLast(): T = data.removeLast()

    protected fun pollFirst(): T? = data.pollFirst()

    protected fun pollLast(): T? = data.pollLast()

    protected fun getFirst(): T = data.first

    protected fun getLast(): T = data.last

    protected fun peekFirst(): T? = data.peekFirst()

    protected fun peekLast(): T? = data.peekLast()

    protected fun removeFirstOccurrence(o: T) = data.removeFirstOccurrence(o)

    protected fun removeLastOccurrence(o: T) = data.removeLastOccurrence(o)

    protected fun pop(): T = data.pop()

    protected fun push(e: T) = data.push(e)

    protected fun offerLast(e: T) = data.offerLast(e)

    protected fun offerFirst(e: T) = data.offerFirst(e)

    protected fun addLast(e: T) = data.addLast(e)

    protected fun addFirst(e: T) = data.addFirst(e)

    protected fun offer(e: T) = data.offer(e)

    protected fun add(element: T) = data.add(element)

    protected fun addAll(elements: Collection<T>) = data.addAll(elements)

    protected fun clear() = data.clear()

    override fun iterator() = data.iterator()

    protected fun remove(): T = data.remove()

    protected fun retainAll(elements: Collection<T>) = data.retainAll(elements.toSet())

    protected fun removeAll(elements: Collection<T>) = data.removeAll(elements.toSet())

    protected fun remove(element: T) = data.remove(element)

    fun lastIndexOf(element: T) = data.lastIndexOf(element)

    fun indexOf(element: T) = data.indexOf(element)

    override fun containsAll(elements: Collection<T>) = data.containsAll(elements)

    override fun contains(element: T) = element in data
}
