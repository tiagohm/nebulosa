package nebulosa.api.atlas

import io.objectbox.Box
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import nebulosa.api.repositories.BoxRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SimbadIdentifierRepository(@Qualifier("simbadIdentifierBox") override val box: Box<SimbadIdentifierEntity>) :
    BoxRepository<SimbadIdentifierEntity>() {

    fun findByName(name: String): List<SimbadIdentifierEntity> {
        return box.query()
            .also {
                if (name.startsWith("%") && name.endsWith("%")) it.contains(SimbadIdentifierEntity_.name, name.replace("%", ""), CASE_INSENSITIVE)
                else if (name.endsWith("%")) it.startsWith(SimbadIdentifierEntity_.name, name.replace("%", ""), CASE_INSENSITIVE)
                else if (name.startsWith("%")) it.endsWith(SimbadIdentifierEntity_.name, name.replace("%", ""), CASE_INSENSITIVE)
                else it.equal(SimbadIdentifierEntity_.name, name, CASE_INSENSITIVE)
            }
            .build()
            .use { it.find() }
    }
}
