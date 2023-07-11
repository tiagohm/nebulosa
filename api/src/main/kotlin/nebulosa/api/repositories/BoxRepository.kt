package nebulosa.api.repositories

import io.objectbox.Box

sealed class BoxRepository<T : Any> : Iterable<T> {

    protected abstract val box: Box<T>

    val size
        get() = box.count().toInt()

    fun isEmpty(): Boolean {
        return box.isEmpty
    }

    override fun iterator(): Iterator<T> {
        return box.all.iterator()
    }

    fun all(): List<T> {
        return box.all
    }

    fun withId(id: Long): T? {
        return box.get(id)
    }

    fun save(entity: T) {
        box.put(entity)
    }

    fun delete(id: Long) {
        box.remove(id)
    }

    fun delete(entity: T) {
        box.remove(entity)
    }

    fun deleteAll() {
        box.removeAll()
    }
}
