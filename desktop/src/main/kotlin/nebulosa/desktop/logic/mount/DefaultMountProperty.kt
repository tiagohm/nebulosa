package nebulosa.desktop.logic.mount

import javafx.beans.property.*
import javafx.collections.FXCollections
import nebulosa.desktop.logic.AbstractDeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.mounts.*
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

    override fun onChanged(prev: Mount?, device: Mount) {
        slewingProperty.set(device.isSlewing)
        trackingProperty.set(device.isTracking)
        slewRatesProperty.setAll(device.slewRates)
        slewRateProperty.set(device.slewRate)
        mountTypeProperty.set(device.mountType)
        trackModesProperty.setAll(device.trackModes)
        trackModeProperty.set(device.trackMode)
        pierSideProperty.set(device.pierSide)
        parkingProperty.set(device.isParking)
        parkedProperty.set(device.isParked)
        canAbortProperty.set(device.canAbort)
        canSyncProperty.set(device.canSync)
        canParkProperty.set(device.canPark)
        guideRateWEProperty.set(device.guideRateWE)
        guideRateNSProperty.set(device.guideRateNS)
        rightAscensionProperty.set(device.rightAscension.hours)
        declinationProperty.set(device.declination.degrees)
        rightAscensionJ2000Property.set(device.rightAscensionJ2000.hours)
        declinationJ2000Property.set(device.declinationJ2000.degrees)
        longitudeProperty.set(device.longitude.degrees)
        latitudeProperty.set(device.latitude.degrees)
        elevationProperty.set(device.elevation.meters)
        timeProperty.set(device.time)
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
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Mount) {
        when (event) {
            is MountEvent -> onChanged(device, device)
        }
    }
}
