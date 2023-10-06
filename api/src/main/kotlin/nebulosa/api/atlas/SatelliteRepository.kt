package nebulosa.api.atlas

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SatelliteRepository : JpaRepository<SatelliteEntity, Long> {

    @Query(
        "SELECT s.* FROM satellites s WHERE" +
                " (:text IS NULL OR s.name LIKE ('%' || :text || '%') OR CAST(s.id AS TEXT) = :text) AND" +
                " (:groupType = 0 OR s.group_type & :groupType != 0)",
        nativeQuery = true,
    )
    fun search(text: String? = null, groupType: Long = 0L, page: Pageable): List<SatelliteEntity>

    fun search(text: String? = null, groups: List<SatelliteGroupType>, page: Pageable): List<SatelliteEntity> {
        return search(text, if (groups.isEmpty()) 0L else SatelliteGroupType.codeOf(groups), page)
    }
}