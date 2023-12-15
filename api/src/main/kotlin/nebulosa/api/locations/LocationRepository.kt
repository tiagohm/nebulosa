package nebulosa.api.locations

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
interface LocationRepository : JpaRepository<LocationEntity, Long> {

    fun findFirstByOrderById(): LocationEntity?

    fun findFirstBySelectedTrueOrderById(): LocationEntity?

    @Modifying
    @Query("UPDATE LocationEntity l SET l.selected = false")
    fun unselectedAll()
}
