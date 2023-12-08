package nebulosa.api.locations

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LocationRepository : JpaRepository<LocationEntity, Long> {

    fun findFirstByOrderById(): LocationEntity?

    fun findFirstBySelectedTrueOrderById(): LocationEntity?

    @Modifying
    @Query("UPDATE LocationEntity l SET l.selected = false")
    fun unselectedAll()
}
