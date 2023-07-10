package nebulosa.api.repositories

import io.objectbox.Box

sealed class BoxRepository<T : Any> {

    protected abstract val box: Box<T>

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
