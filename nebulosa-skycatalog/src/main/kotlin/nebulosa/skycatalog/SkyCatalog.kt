package nebulosa.skycatalog

import nebulosa.erfa.CartesianCoordinate
import nebulosa.erfa.SphericalCoordinate
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Vector3D
import java.util.*

abstract class SkyCatalog : Collection<SkyObject> {

    private val data = LinkedList<SkyObject>()

    fun searchAround(vector: Vector3D, limitFOV: Angle): List<SkyObject> {
        val res = ArrayList<SkyObject>()
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

    fun searchAround(coordinate: SphericalCoordinate, limitFOV: Angle): List<SkyObject> {
        return searchAround(coordinate.cartesian, limitFOV)
    }

    fun searchAround(coordinate: CartesianCoordinate, limitFOV: Angle): List<SkyObject> {
        return searchAround(coordinate.vector, limitFOV)
    }

    fun searchAround(rightAscension: Angle, declination: Angle, limitFOV: Angle): List<SkyObject> {
        return searchAround(SphericalCoordinate(rightAscension, declination, Distance.ONE), limitFOV)
    }

    override val size
        get() = data.size

    operator fun get(index: Int) = data[index]

    override fun isEmpty() = data.isEmpty()

    protected fun poll(): SkyObject? = data.poll()

    protected fun element(): SkyObject = data.element()

    protected fun peek(): SkyObject? = data.peek()

    protected fun removeFirst(): SkyObject = data.removeFirst()

    protected fun removeLast(): SkyObject = data.removeLast()

    protected fun pollFirst(): SkyObject? = data.pollFirst()

    protected fun pollLast(): SkyObject? = data.pollLast()

    protected fun getFirst(): SkyObject = data.first

    protected fun getLast(): SkyObject = data.last

    protected fun peekFirst(): SkyObject? = data.peekFirst()

    protected fun peekLast(): SkyObject? = data.peekLast()

    protected fun removeFirstOccurrence(o: SkyObject) = data.removeFirstOccurrence(o)

    protected fun removeLastOccurrence(o: SkyObject) = data.removeLastOccurrence(o)

    protected fun pop(): SkyObject = data.pop()

    protected fun push(e: SkyObject) = data.push(e)

    protected fun offerLast(e: SkyObject) = data.offerLast(e)

    protected fun offerFirst(e: SkyObject) = data.offerFirst(e)

    protected fun addLast(e: SkyObject) = data.addLast(e)

    protected fun addFirst(e: SkyObject) = data.addFirst(e)

    protected fun offer(e: SkyObject) = data.offer(e)

    protected fun add(element: SkyObject) = data.add(element)

    protected fun addAll(elements: Collection<SkyObject>) = data.addAll(elements)

    protected fun clear() = data.clear()

    override fun iterator() = data.iterator()

    protected fun remove(): SkyObject = data.remove()

    protected fun retainAll(elements: Collection<SkyObject>) = data.retainAll(elements.toSet())

    protected fun removeAll(elements: Collection<SkyObject>) = data.removeAll(elements.toSet())

    protected fun remove(element: SkyObject) = data.remove(element)

    fun lastIndexOf(element: SkyObject) = data.lastIndexOf(element)

    fun indexOf(element: SkyObject) = data.indexOf(element)

    override fun containsAll(elements: Collection<SkyObject>) = data.containsAll(elements)

    override fun contains(element: SkyObject) = element in data
}
