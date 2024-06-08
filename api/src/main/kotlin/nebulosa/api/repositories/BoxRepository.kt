package nebulosa.api.repositories

import io.objectbox.Box
import io.objectbox.Property
import io.objectbox.kotlin.and
import io.objectbox.kotlin.equal
import io.objectbox.kotlin.or
import io.objectbox.query.PropertyQueryCondition
import io.objectbox.query.QueryBuilder.StringOrder
import io.objectbox.query.QueryCondition
import nebulosa.api.database.BoxEntity

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
        return box.query().build().use { it.findLazy() }
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
        return findAll().iterator()
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { it in this }
    }

    override fun contains(element: T): Boolean {
        return element.id in box
    }

    companion object {

        inline val <T> Property<T>.isTrue
            get() = this equal true

        inline val <T> Property<T>.isFalse
            get() = this equal false

        @Suppress("NOTHING_TO_INLINE")
        inline infix fun <T> Property<T>.equalInsensitive(value: String): PropertyQueryCondition<T> {
            return equal(value, StringOrder.CASE_INSENSITIVE)
        }

        @Suppress("NOTHING_TO_INLINE")
        inline infix fun <T> Property<T>.containsInsensitive(value: String): PropertyQueryCondition<T> {
            return contains(value, StringOrder.CASE_INSENSITIVE)
        }

        @JvmStatic
        fun <T> and(condition: QueryCondition<T>, vararg conditions: QueryCondition<T>?): QueryCondition<T> {
            return conditions.fold(condition) { a, b -> if (b == null) a else a and b }
        }

        @JvmStatic
        fun <T> and(vararg conditions: QueryCondition<T>?): QueryCondition<T>? {
            return if (conditions.isEmpty()) null else conditions.reduce { a, b -> if (b == null) a else a?.and(b) ?: b }
        }

        @JvmStatic
        fun <T> and(conditions: Collection<QueryCondition<T>?>): QueryCondition<T>? {
            return if (conditions.isEmpty()) null else conditions.reduce { a, b -> if (b == null) a else a?.and(b) ?: b }
        }

        @JvmStatic
        fun <T> or(condition: QueryCondition<T>, vararg conditions: QueryCondition<T>?): QueryCondition<T> {
            return conditions.fold(condition) { a, b -> if (b == null) a else a or b }
        }

        @JvmStatic
        fun <T> or(vararg conditions: QueryCondition<T>?): QueryCondition<T>? {
            return if (conditions.isEmpty()) null else conditions.reduce { a, b -> if (b == null) a else a?.or(b) ?: b }
        }

        @JvmStatic
        fun <T> or(conditions: Collection<QueryCondition<T>?>): QueryCondition<T>? {
            return if (conditions.isEmpty()) null else conditions.reduce { a, b -> if (b == null) a else a?.or(b) ?: b }
        }
    }
}
