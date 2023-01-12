package nebulosa.desktop.equipments

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.mounts.*
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
    @JvmField val trackMode = SimpleObjectProperty(TrackMode.SIDEREAL)
    @JvmField val pierSide = SimpleObjectProperty(PierSide.NEITHER)
    @JvmField val canAbort = SimpleBooleanProperty()
    @JvmField val canSync = SimpleBooleanProperty()
    @JvmField val canPark = SimpleBooleanProperty()
    @JvmField val guideRateWE = SimpleDoubleProperty()
    @JvmField val guideRateNS = SimpleDoubleProperty()
    @JvmField val rightAscension = SimpleDoubleProperty()
    @JvmField val declination = SimpleDoubleProperty()
    @JvmField val longitude = SimpleDoubleProperty()
    @JvmField val latitude = SimpleDoubleProperty()
    @JvmField val elevation = SimpleDoubleProperty()
    @JvmField val time = SimpleObjectProperty(OffsetDateTime.now())

    override fun changed(value: Mount) {
        isSlewing.set(value.isSlewing)
        isTracking.set(value.isTracking)
        slewRates.setAll(value.slewRates)
        slewRate.set(value.slewRate)
        mountType.set(value.mountType)
        trackModes.setAll(value.trackModes)
        trackMode.set(value.trackMode)
        pierSide.set(value.pierSide)
        isParking.set(value.isParking)
        isParked.set(value.isParked)
        canAbort.set(value.canAbort)
        canSync.set(value.canSync)
        canPark.set(value.canPark)
        guideRateWE.set(value.guideRateWE)
        guideRateNS.set(value.guideRateNS)
        rightAscension.set(value.rightAscension)
        declination.set(value.declination)
        longitude.set(value.longitude.degrees)
        latitude.set(value.latitude.degrees)
        elevation.set(value.elevation.meters)
        time.set(value.time)
    }

    override fun reset() {
        isSlewing.set(false)
        isTracking.set(false)
        slewRates.clear()
        slewRate.set(null)
        mountType.set(MountType.EQ_GEM)
        trackModes.clear()
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
        longitude.set(0.0)
        latitude.set(0.0)
        elevation.set(0.0)
        time.set(OffsetDateTime.now())
    }

    override fun accept(event: DeviceEvent<Mount>) {
        val device = event.device!!

        when (event) {
            is MountSlewingChanged -> Platform.runLater { isSlewing.set(device.isSlewing) }
            is MountSlewRatesChanged -> Platform.runLater {
                // Workaround: choicebox removes the selected item when call setAll.
                // So, unselect it to not be removed.
                // Bug: Disconnect and reconnect, the choicebox selected value is null/empty.
                slewRate.set(null)
                slewRates.setAll(device.slewRates)
            }
            is MountSlewRateChanged -> Platform.runLater { slewRate.set(device.slewRate) }
            is MountTypeChanged -> Platform.runLater { mountType.set(device.mountType) }
            is MountTrackModesChanged -> Platform.runLater {
                trackMode.set(null)
                trackModes.setAll(device.trackModes)
            }
            is MountTrackModeChanged -> Platform.runLater { trackMode.set(device.trackMode) }
            is MountTrackingChanged -> Platform.runLater { isTracking.set(device.isTracking) }
            is MountPierSideChanged -> Platform.runLater { pierSide.set(device.pierSide) }
            is MountCanAbortChanged -> Platform.runLater { canAbort.set(device.canAbort) }
            is MountCanSyncChanged -> Platform.runLater { canSync.set(device.canSync) }
            is MountCanParkChanged -> Platform.runLater { canPark.set(device.canPark) }
            is MountEquatorialCoordinatesChanged -> Platform.runLater {
                rightAscension.set(device.rightAscension)
                declination.set(device.declination)
            }
            is MountGuideRateChanged -> Platform.runLater {
                guideRateWE.set(device.guideRateWE)
                guideRateNS.set(device.guideRateNS)
            }
            is MountParkChanged -> Platform.runLater {
                isParking.set(device.isParking)
                isParked.set(device.isParked)
            }
            is MountCoordinateChanged -> Platform.runLater {
                longitude.set(device.longitude.degrees)
                latitude.set(device.latitude.degrees)
                elevation.set(device.elevation.meters)
            }
            is MountTimeChanged -> Platform.runLater {
                time.set(device.time)
            }
        }
    }
}
