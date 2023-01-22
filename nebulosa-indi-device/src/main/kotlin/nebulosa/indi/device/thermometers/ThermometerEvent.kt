package nebulosa.indi.device.thermometers

import nebulosa.indi.device.DeviceEvent

interface ThermometerEvent<T : Thermometer> : DeviceEvent<T>
