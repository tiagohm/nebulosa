package nebulosa.desktop.service

import nebulosa.desktop.data.DeepSkyObjectEntity
import nebulosa.desktop.data.StarEntity
import nebulosa.desktop.repository.sky.DeepSkyObjectRepository
import nebulosa.desktop.repository.sky.StarRepository
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SkyObjectService {

    data class Filter(
        val rightAscension: Angle = Angle.ZERO,
        val declination: Angle = Angle.ZERO,
        val radius: Angle = Angle.ZERO,
        val constellation: Constellation? = null,
        val magnitudeMin: Double = -SkyObject.UNKNOWN_MAGNITUDE,
        val magnitudeMax: Double = SkyObject.UNKNOWN_MAGNITUDE,
        val type: SkyObjectType? = null,
    ) {

        companion object {

            @JvmStatic val EMPTY = Filter()
        }
    }

    @Autowired private lateinit var deepSkyObjectRepository: DeepSkyObjectRepository

    @Autowired private lateinit var starRepository: StarRepository

    fun searchDSO(text: String, filter: Filter): List<DeepSkyObjectEntity> {
        return deepSkyObjectRepository.search(
            text.ifBlank { null }?.let { "%$it%" },
            filter.rightAscension, filter.declination, filter.radius,
            filter.constellation, filter.magnitudeMin.clampInRange(), filter.magnitudeMax.clampInRange(), filter.type,
            Pageable.ofSize(1000),
        )
    }

    fun searchStar(text: String, filter: Filter): List<StarEntity> {
        return starRepository.search(
            text.ifBlank { null }?.let { "%$it%" },
            filter.rightAscension, filter.declination, filter.radius,
            filter.constellation, filter.magnitudeMin.clampInRange(), filter.magnitudeMax.clampInRange(), filter.type,
            Pageable.ofSize(1000),
        )
    }

    companion object {

        @JvmStatic private val MAGNITUDE_RANGE = -29.9..29.9

        @JvmStatic
        private fun Double.clampInRange(range: ClosedFloatingPointRange<Double> = MAGNITUDE_RANGE): Double {
            return if (this in range) this
            else if (this < 0.0) -SkyObject.UNKNOWN_MAGNITUDE
            else SkyObject.UNKNOWN_MAGNITUDE
        }
    }
}
