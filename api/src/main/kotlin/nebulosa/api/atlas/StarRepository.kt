package nebulosa.api.atlas

import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
interface StarRepository : JpaRepository<StarEntity, Long> {

    @Query(
        "SELECT star FROM StarEntity star WHERE " +
                "(:text IS NULL OR star.name LIKE CONCAT('%', :text, '%')) AND " +
                "(:constellation IS NULL OR star.constellation = :constellation) AND " +
                "(:type IS NULL OR star.type = :type) AND " +
                "(star.magnitude BETWEEN :magnitudeMin AND :magnitudeMax) AND " +
                "(:radius <= 0.0 OR acos(sin(star.declinationJ2000) * sin(:declinationJ2000) + cos(star.declinationJ2000) * cos(:declinationJ2000) * cos(star.rightAscensionJ2000 - :rightAscensionJ2000)) <= :radius) " +
                "ORDER BY star.magnitude ASC"
    )
    @Transactional(readOnly = true)
    fun search(
        text: String? = null,
        rightAscensionJ2000: Angle = 0.0, declinationJ2000: Angle = 0.0, radius: Angle = 0.0,
        constellation: Constellation? = null,
        magnitudeMin: Double = -SkyObject.UNKNOWN_MAGNITUDE, magnitudeMax: Double = SkyObject.UNKNOWN_MAGNITUDE,
        type: SkyObjectType? = null,
        pageable: Pageable = Pageable.unpaged(),
    ): List<StarEntity>

    @Query("SELECT DISTINCT star.type FROM StarEntity star")
    fun types(): List<SkyObjectType>
}
