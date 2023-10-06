package nebulosa.api.atlas

import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DeepSkyObjectRepository : JpaRepository<DeepSkyObjectEntity, Long> {

    @Query(
        "SELECT dso FROM DeepSkyObjectEntity dso WHERE " +
                "(:text IS NULL OR dso.name LIKE CONCAT('%', :text, '%')) AND " +
                "(:constellation IS NULL OR dso.constellation = :constellation) AND " +
                "(:type IS NULL OR dso.type = :type) AND " +
                "(dso.magnitude BETWEEN :magnitudeMin AND :magnitudeMax) AND " +
                "(:radius <= 0.0 OR acos(sin(dso.declinationJ2000) * sin(:declinationJ2000) + cos(dso.declinationJ2000) * cos(:declinationJ2000) * cos(dso.rightAscensionJ2000 - :rightAscensionJ2000)) <= :radius) " +
                "ORDER BY dso.magnitude ASC"
    )
    fun search(
        text: String? = null,
        rightAscensionJ2000: Angle = 0.0, declinationJ2000: Angle = 0.0, radius: Angle = 0.0,
        constellation: Constellation? = null,
        magnitudeMin: Double = -100.0, magnitudeMax: Double = 100.0,
        type: SkyObjectType? = null,
        pageable: Pageable = Pageable.unpaged(),
    ): List<DeepSkyObjectEntity>
}