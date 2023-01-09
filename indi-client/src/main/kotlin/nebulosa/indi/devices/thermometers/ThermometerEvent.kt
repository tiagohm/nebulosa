package nebulosa.indi.devices.thermometers

import nebulosa.indi.devices.DeviceEvent

interface ThermometerEvent<T : Thermometer> : DeviceEvent<T>
