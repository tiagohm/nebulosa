package nebulosa.api.repositories

import io.objectbox.Box
import nebulosa.api.entities.BoxEntity

abstract class BoxRepository<T : BoxEntity> : Collection<T> {

    protected abstract val box: Box<T>

    @Synchronized
    open fun save(entity: T): T {
        return entity.also(box::put)
    }

    @Synchronized
    open fun save(entities: Collection<T>): Collection<T> {
        return entities.also(box::put)
    }

    @Synchronized
    open fun delete(entity: T): Boolean {
        return box.remove(entity.id)
    }

    @Synchronized
    open fun delete(id: Long): Boolean {
        return box.remove(id)
    }

    @Synchronized
    open fun deleteAll() {
        return box.removeAll()
    }

    open fun findAll(): List<T> {
        return box.all
    }

    open fun find(id: Long): T? {
        return box.get(id)
    }

    override val size
        get() = box.count().toInt()

    override fun isEmpty(): Boolean {
        return box.isEmpty
    }

    override fun iterator(): Iterator<T> {
        return box.all.iterator()
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { it in this }
    }

    override fun contains(element: T): Boolean {
        return element.id in box
    }
}
