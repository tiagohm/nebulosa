package nebulosa.api.atlas

import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StarRepository : JpaRepository<StarEntity, Long> {

    @Query(
        "SELECT star FROM StarEntity star WHERE " +
                "(:text IS NULL OR star.names LIKE CONCAT('%', :text, '%')) AND " +
                "(:constellation IS NULL OR star.constellation = :constellation) AND " +
                "(:type IS NULL OR star.type = :type) AND " +
                "(star.magnitude BETWEEN :magnitudeMin AND :magnitudeMax) AND " +
                "(:radius <= 0.0 OR acos(sin(star.declinationJ2000) * sin(:declinationJ2000) + cos(star.declinationJ2000) * cos(:declinationJ2000) * cos(star.rightAscensionJ2000 - :rightAscensionJ2000)) <= :radius) " +
                "ORDER BY star.magnitude ASC"
    )
    fun search(
        text: String? = null,
        rightAscensionJ2000: Angle = Angle.ZERO, declinationJ2000: Angle = Angle.ZERO, radius: Angle = Angle.ZERO,
        constellation: Constellation? = null,
        magnitudeMin: Double = -100.0, magnitudeMax: Double = 100.0,
        type: SkyObjectType? = null,
        pageable: Pageable = Pageable.unpaged(),
    ): List<StarEntity>
}
