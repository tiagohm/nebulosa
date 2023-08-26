package nebulosa.api.repositories

import io.objectbox.BoxStore
import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.TLESourceEntity
import org.springframework.stereotype.Service

@Service
class TLESourceRepository(boxStore: BoxStore) : BoxRepository<TLESourceEntity>() {

    override val box = boxStore.boxFor(TLESourceEntity::class.java)!!

    @PostConstruct
    fun createIfEmpty() {
        if (isEmpty()) {
            for ((source, enabled) in DEFAULT_SOURCE_URLS) {
                save(TLESourceEntity(url = source, enabled = enabled))
            }
        }
    }

    companion object {

        const val CELESTRAK_URL = "https://celestrak.org/NORAD/elements/gp.php"

        @JvmStatic private val DEFAULT_SOURCE_URLS = mapOf(
            "$CELESTRAK_URL?GROUP=1982-092&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=1999-025&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=active&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=amateur&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=analyst&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=argos&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=beidou&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=cosmos-2251-debris&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=cubesat&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=dmc&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=education&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=engineering&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=galileo&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=geo&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=geodetic&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=glo-ops&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=globalstar&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=gnss&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=goes&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=gorizont&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=gps-ops&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=intelsat&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=iridium&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=iridium-33-debris&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=iridium-NEXT&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=last-30-days&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=military&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=molniya&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=musson&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=nnss&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=noaa&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=oneweb&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=orbcomm&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=other&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=other-comm&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=planet&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=radar&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=raduga&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=resource&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=sarsat&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=satnogs&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=sbas&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=science&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=ses&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=spire&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=starlink&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=stations&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=swarm&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=tdrss&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=visual&FORMAT=tle" to true,
            "$CELESTRAK_URL?GROUP=weather&FORMAT=tle" to false,
            "$CELESTRAK_URL?GROUP=x-comm&FORMAT=tle" to false,
        )
    }
}
