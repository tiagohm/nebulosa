import { EMPTY_DEVICE_SENDER, EMPTY_DRIVER_INFO, type Device } from './device.types'

export interface LightBox extends Device {
	enabled: boolean
	intensity: number
	minIntensity: number
	maxIntensity: number
}

export const DEFAULT_LIGHT_BOX: LightBox = {
	sender: EMPTY_DEVICE_SENDER,
	driver: EMPTY_DRIVER_INFO,
	enabled: false,
	intensity: 0,
	minIntensity: 0,
	maxIntensity: 0,
	type: 'LIGHT_BOX',
	id: '',
	name: '',
	connected: false,
}

export function isLightBox(device?: Device): device is LightBox {
	return !!device && device.type === 'LIGHT_BOX'
}
