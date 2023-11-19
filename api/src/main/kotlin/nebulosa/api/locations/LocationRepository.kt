package nebulosa.api.locations

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
interface LocationRepository : JpaRepository<LocationEntity, Long>
