import { Device } from './device.types'

export interface Thermometer extends Device {
	hasThermometer: boolean
	temperature: number
}

export function isThermometer(device?: Device): device is Thermometer {
	return !!device && 'temperature' in device
}
