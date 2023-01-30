package nebulosa.desktop.logic.mount

import javafx.beans.property.*
import javafx.collections.FXCollections
import nebulosa.desktop.logic.AbstractDeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.mount.*
import java.time.OffsetDateTime

open class DefaultMountProperty : AbstractDeviceProperty<Mount>(), MountProperty {

    override val slewingProperty = SimpleBooleanProperty()
    override val trackingProperty = SimpleBooleanProperty()
    override val parkingProperty = SimpleBooleanProperty()
    override val parkedProperty = SimpleBooleanProperty()
    override val slewRatesProperty = SimpleListProperty(FXCollections.observableArrayList<String>())
    override val slewRateProperty = SimpleStringProperty()
    override val mountTypeProperty = SimpleObjectProperty(MountType.EQ_GEM)
    override val trackModesProperty = SimpleListProperty(FXCollections.observableArrayList<TrackMode>())
    override val trackModeProperty = SimpleObjectProperty<TrackMode>()
    override val pierSideProperty = SimpleObjectProperty(PierSide.NEITHER)
    override val canAbortProperty = SimpleBooleanProperty()
    override val canSyncProperty = SimpleBooleanProperty()
    override val canParkProperty = SimpleBooleanProperty()
    override val guideRateWEProperty = SimpleDoubleProperty()
    override val guideRateNSProperty = SimpleDoubleProperty()
    override val rightAscensionProperty = SimpleDoubleProperty()
    override val declinationProperty = SimpleDoubleProperty()
    override val rightAscensionJ2000Property = SimpleDoubleProperty()
    override val declinationJ2000Property = SimpleDoubleProperty()
    override val longitudeProperty = SimpleDoubleProperty()
    override val latitudeProperty = SimpleDoubleProperty()
    override val elevationProperty = SimpleDoubleProperty()
    override val timeProperty = SimpleObjectProperty(OffsetDateTime.now())
    override val azimuthProperty = SimpleDoubleProperty()
    override val altitudeProperty = SimpleDoubleProperty()

    override fun onChanged(prev: Mount?, device: Mount) {
        slewingProperty.set(device.slewing)
        trackingProperty.set(device.tracking)
        slewRatesProperty.setAll(device.slewRates)
        slewRateProperty.set(device.slewRate)
        mountTypeProperty.set(device.mountType)
        trackModesProperty.setAll(device.trackModes)
        trackModeProperty.set(device.trackMode)
        pierSideProperty.set(device.pierSide)
        parkingProperty.set(device.parking)
        parkedProperty.set(device.parked)
        canAbortProperty.set(device.canAbort)
        canSyncProperty.set(device.canSync)
        canParkProperty.set(device.canPark)
        guideRateWEProperty.set(device.guideRateWE)
        guideRateNSProperty.set(device.guideRateNS)
        rightAscensionProperty.set(device.rightAscension.hours)
        declinationProperty.set(device.declination.degrees)
        longitudeProperty.set(device.longitude.degrees)
        latitudeProperty.set(device.latitude.degrees)
        elevationProperty.set(device.elevation.meters)
        timeProperty.set(device.time)

        computeCoordinates()
    }

    override fun onReset() {
        slewingProperty.set(false)
        trackingProperty.set(false)
        slewRatesProperty.clear()
        slewRateProperty.set(null)
        mountTypeProperty.set(MountType.EQ_GEM)
        trackModesProperty.setAll(listOf(TrackMode.SIDEREAL))
        trackModeProperty.set(TrackMode.SIDEREAL)
        pierSideProperty.set(PierSide.NEITHER)
        parkingProperty.set(false)
        parkedProperty.set(false)
        canAbortProperty.set(false)
        canSyncProperty.set(false)
        canParkProperty.set(false)
        guideRateWEProperty.set(0.0)
        guideRateNSProperty.set(0.0)
        rightAscensionProperty.set(0.0)
        declinationProperty.set(0.0)
        rightAscensionJ2000Property.set(0.0)
        declinationJ2000Property.set(0.0)
        longitudeProperty.set(0.0)
        latitudeProperty.set(0.0)
        elevationProperty.set(0.0)
        timeProperty.set(OffsetDateTime.now())
        azimuthProperty.set(0.0)
        altitudeProperty.set(0.0)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Mount) {
        when (event) {
            is MountSlewingChanged -> slewingProperty.set(device.slewing)
            is MountSlewRateChanged -> slewRateProperty.set(device.slewRate)
            is MountTypeChanged -> mountTypeProperty.set(device.mountType)
            is MountTrackModeChanged -> trackModeProperty.set(device.trackMode)
            is MountTrackingChanged -> trackingProperty.set(device.tracking)
            is MountPierSideChanged -> pierSideProperty.set(device.pierSide)
            is MountCanAbortChanged -> canAbortProperty.set(device.canAbort)
            is MountCanSyncChanged -> canSyncProperty.set(device.canSync)
            is MountCanParkChanged -> canParkProperty.set(device.canPark)
            is MountTimeChanged -> timeProperty.set(device.time)
            is MountSlewRatesChanged -> {
                slewRateProperty.set(null)
                slewRatesProperty.setAll(device.slewRates)
                slewRateProperty.set(device.slewRate ?: device.slewRates.firstOrNull())
            }
            is MountTrackModesChanged -> {
                trackModeProperty.set(null)
                trackModesProperty.setAll(device.trackModes)
                trackModeProperty.set(device.trackMode)
            }
            is MountEquatorialCoordinatesChanged -> {
                rightAscensionProperty.set(device.rightAscension.hours)
                declinationProperty.set(device.declination.degrees)
            }
            is MountGuideRateChanged -> {
                guideRateWEProperty.set(device.guideRateWE)
                guideRateNSProperty.set(device.guideRateNS)
            }
            is MountParkChanged -> {
                parkingProperty.set(device.parking)
                parkedProperty.set(device.parked)
            }
            is MountGeographicCoordinateChanged -> {
                longitudeProperty.set(device.longitude.degrees)
                latitudeProperty.set(device.latitude.degrees)
                elevationProperty.set(device.elevation.meters)
            }
        }
    }
}
