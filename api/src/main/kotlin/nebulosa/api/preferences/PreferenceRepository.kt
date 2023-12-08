package nebulosa.api.preferences

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PreferenceRepository : JpaRepository<PreferenceEntity, String> {

    @Query("SELECT p.key FROM PreferenceEntity p")
    fun keys(): Set<String>
}
