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
                "(:radius <= 0.0 OR acos(sin(star.declination) * sin(:declination) + cos(star.declination) * cos(:declination) * cos(star.rightAscension - :rightAscension)) <= :radius) " +
                "ORDER BY star.magnitude ASC"
    )
    fun search(
        text: String? = null,
        rightAscension: Angle = Angle.ZERO, declination: Angle = Angle.ZERO, radius: Angle = Angle.ZERO,
        constellation: Constellation? = null,
        magnitudeMin: Double = -100.0, magnitudeMax: Double = 100.0,
        type: SkyObjectType? = null,
        pageable: Pageable = Pageable.unpaged(),
    ): List<StarEntity>
}
