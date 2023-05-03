package nebulosa.desktop.repository.sky

import nebulosa.desktop.data.DeepSkyObjectEntity
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
interface DeepSkyObjectRepository : JpaRepository<DeepSkyObjectEntity, Int> {

    @Query(
        "SELECT dso FROM DeepSkyObjectEntity dso WHERE " +
                "(:text IS NULL OR dso.names LIKE :text) AND " +
                "(:constellation IS NULL OR dso.constellation = :constellation) AND " +
                "(:type IS NULL OR dso.type = :type) AND " +
                "(dso.magnitude BETWEEN :magnitudeMin AND :magnitudeMax) AND " +
                "(:radius <= 0.0 OR acos(sin(dso.declination) * sin(:declination) + cos(dso.declination) * cos(:declination) * cos(dso.rightAscension - :rightAscension)) <= :radius) " +
                "ORDER BY dso.magnitude ASC"
    )
    fun search(
        text: String? = null,
        rightAscension: Angle = Angle.ZERO, declination: Angle = Angle.ZERO, radius: Angle = Angle.ZERO,
        constellation: Constellation? = null,
        magnitudeMin: Double = -100.0, magnitudeMax: Double = 100.0,
        type: SkyObjectType? = null,
        pageable: Pageable = Pageable.unpaged(),
    ): List<DeepSkyObjectEntity>
}
