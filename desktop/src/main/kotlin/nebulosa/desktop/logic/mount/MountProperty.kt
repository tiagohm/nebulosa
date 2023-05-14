package nebulosa.desktop.logic.mount

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.mount.*
import nebulosa.nova.astrometry.Constellation
import java.time.OffsetDateTime

interface MountProperty : DeviceProperty<Mount> {

    val slewingProperty: SimpleBooleanProperty

    val trackingProperty: SimpleBooleanProperty

    val parkingProperty: SimpleBooleanProperty

    val parkedProperty: SimpleBooleanProperty

    val slewRatesProperty: SimpleListProperty<SlewRate>

    val slewRateProperty: SimpleObjectProperty<SlewRate>

    val mountTypeProperty: SimpleObjectProperty<MountType>

    val trackModesProperty: SimpleListProperty<TrackMode>

    val trackModeProperty: SimpleObjectProperty<TrackMode>

    val pierSideProperty: SimpleObjectProperty<PierSide>

    val canAbortProperty: SimpleBooleanProperty

    val canSyncProperty: SimpleBooleanProperty

    val canGoToProperty: SimpleBooleanProperty

    val canParkProperty: SimpleBooleanProperty

    val canHomeProperty: SimpleBooleanProperty

    val guideRateWEProperty: SimpleDoubleProperty

    val guideRateNSProperty: SimpleDoubleProperty

    val rightAscensionProperty: SimpleDoubleProperty

    val declinationProperty: SimpleDoubleProperty

    val rightAscensionJ2000Property: SimpleDoubleProperty

    val declinationJ2000Property: SimpleDoubleProperty

    val longitudeProperty: SimpleDoubleProperty

    val latitudeProperty: SimpleDoubleProperty

    val elevationProperty: SimpleDoubleProperty

    val timeProperty: SimpleObjectProperty<OffsetDateTime>

    val azimuthProperty: SimpleDoubleProperty

    val altitudeProperty: SimpleDoubleProperty

    val constellationProperty: SimpleObjectProperty<Constellation>

    val slewing
        get() = slewingProperty.get()

    val tracking
        get() = trackingProperty.get()

    val parking
        get() = parkingProperty.get()

    val parked
        get() = parkedProperty.get()

    val slewRates: List<SlewRate>
        get() = slewRatesProperty.get()

    val slewRate: SlewRate?
        get() = slewRateProperty.get()

    val mountType: MountType?
        get() = mountTypeProperty.get()

    val trackModes: List<TrackMode>
        get() = trackModesProperty.get()

    val trackMode: TrackMode?
        get() = trackModeProperty.get()

    val pierSide: PierSide?
        get() = pierSideProperty.get()

    val canAbort
        get() = canAbortProperty.get()

    val canSync
        get() = canSyncProperty.get()

    val canGoTo
        get() = canGoToProperty.get()

    val canPark
        get() = canParkProperty.get()

    val guideRateWE
        get() = guideRateWEProperty.get()

    val guideRateNS
        get() = guideRateNSProperty.get()

    val rightAscension
        get() = rightAscensionProperty.get()

    val declination
        get() = declinationProperty.get()

    val rightAscensionJ2000
        get() = rightAscensionJ2000Property.get()

    val declinationJ2000
        get() = declinationJ2000Property.get()

    val constellation: Constellation?
        get() = constellationProperty.get()

    val longitude
        get() = longitudeProperty.get()

    val latitude
        get() = latitudeProperty.get()

    val elevation
        get() = elevationProperty.get()

    val time: OffsetDateTime?
        get() = timeProperty.get()
}
