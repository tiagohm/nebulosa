package nebulosa.desktop.logic.mount

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.mounts.*
import java.time.OffsetDateTime

class MountProperty : DeviceProperty<Mount>() {

    @JvmField val isSlewing = SimpleBooleanProperty()
    @JvmField val isTracking = SimpleBooleanProperty()
    @JvmField val isParking = SimpleBooleanProperty()
    @JvmField val isParked = SimpleBooleanProperty()
    @JvmField val slewRates = SimpleListProperty(FXCollections.observableArrayList<String>())
    @JvmField val slewRate = SimpleObjectProperty<String>()
    @JvmField val mountType = SimpleObjectProperty(MountType.EQ_GEM)
    @JvmField val trackModes = SimpleListProperty(FXCollections.observableArrayList<TrackMode>())
    @JvmField val trackMode = SimpleObjectProperty<TrackMode>()
    @JvmField val pierSide = SimpleObjectProperty(PierSide.NEITHER)
    @JvmField val canAbort = SimpleBooleanProperty()
    @JvmField val canSync = SimpleBooleanProperty()
    @JvmField val canPark = SimpleBooleanProperty()
    @JvmField val guideRateWE = SimpleDoubleProperty()
    @JvmField val guideRateNS = SimpleDoubleProperty()
    @JvmField val rightAscension = SimpleDoubleProperty()
    @JvmField val declination = SimpleDoubleProperty()
    @JvmField val rightAscensionJ2000 = SimpleDoubleProperty()
    @JvmField val declinationJ2000 = SimpleDoubleProperty()
    @JvmField val longitude = SimpleDoubleProperty()
    @JvmField val latitude = SimpleDoubleProperty()
    @JvmField val elevation = SimpleDoubleProperty()
    @JvmField val time = SimpleObjectProperty(OffsetDateTime.now())

    override fun onChanged(prev: Mount?, new: Mount) {
        isSlewing.set(new.isSlewing)
        isTracking.set(new.isTracking)
        slewRates.setAll(new.slewRates)
        slewRate.set(new.slewRate)
        mountType.set(new.mountType)
        trackModes.setAll(new.trackModes)
        trackMode.set(new.trackMode)
        pierSide.set(new.pierSide)
        isParking.set(new.isParking)
        isParked.set(new.isParked)
        canAbort.set(new.canAbort)
        canSync.set(new.canSync)
        canPark.set(new.canPark)
        guideRateWE.set(new.guideRateWE)
        guideRateNS.set(new.guideRateNS)
        rightAscension.set(new.rightAscension.hours)
        declination.set(new.declination.degrees)
        rightAscensionJ2000.set(new.rightAscensionJ2000.hours)
        declinationJ2000.set(new.declinationJ2000.degrees)
        longitude.set(new.longitude.degrees)
        latitude.set(new.latitude.degrees)
        elevation.set(new.elevation.meters)
        time.set(new.time)
    }

    override fun reset() {
        isSlewing.set(false)
        isTracking.set(false)
        slewRates.clear()
        slewRate.set(null)
        mountType.set(MountType.EQ_GEM)
        trackModes.setAll(listOf(TrackMode.SIDEREAL))
        trackMode.set(TrackMode.SIDEREAL)
        pierSide.set(PierSide.NEITHER)
        isParking.set(false)
        isParked.set(false)
        canAbort.set(false)
        canSync.set(false)
        canPark.set(false)
        guideRateWE.set(0.0)
        guideRateNS.set(0.0)
        rightAscension.set(0.0)
        declination.set(0.0)
        rightAscensionJ2000.set(0.0)
        declinationJ2000.set(0.0)
        longitude.set(0.0)
        latitude.set(0.0)
        elevation.set(0.0)
        time.set(OffsetDateTime.now())
    }

    override fun onDeviceEvent(event: DeviceEvent<*>) {
        super.onDeviceEvent(event)

        when (event) {
            is MountSlewingChanged -> isSlewing.set(value.isSlewing)
            is MountSlewRatesChanged -> {
                slewRate.set(null)
                slewRates.setAll(value.slewRates)
                slewRate.set(value.slewRate ?: value.slewRates.firstOrNull())
            }
            is MountSlewRateChanged -> slewRate.set(value.slewRate)
            is MountTypeChanged -> mountType.set(value.mountType)
            is MountTrackModesChanged -> {
                trackMode.set(null)
                trackModes.setAll(value.trackModes)
                trackMode.set(value.trackMode)
            }
            is MountTrackModeChanged -> trackMode.set(value.trackMode)
            is MountTrackingChanged -> isTracking.set(value.isTracking)
            is MountPierSideChanged -> pierSide.set(value.pierSide)
            is MountCanAbortChanged -> canAbort.set(value.canAbort)
            is MountCanSyncChanged -> canSync.set(value.canSync)
            is MountCanParkChanged -> canPark.set(value.canPark)
            is MountEquatorialCoordinatesChanged -> {
                rightAscension.set(value.rightAscension.hours)
                declination.set(value.declination.degrees)
                rightAscensionJ2000.set(value.rightAscensionJ2000.hours)
                declinationJ2000.set(value.declinationJ2000.degrees)
            }
            is MountGuideRateChanged -> {
                guideRateWE.set(value.guideRateWE)
                guideRateNS.set(value.guideRateNS)
            }
            is MountParkChanged -> {
                isParking.set(value.isParking)
                isParked.set(value.isParked)
            }
            is MountCoordinateChanged -> {
                longitude.set(value.longitude.degrees)
                latitude.set(value.latitude.degrees)
                elevation.set(value.elevation.meters)
            }
            is MountTimeChanged -> time.set(value.time)
        }
    }
}
