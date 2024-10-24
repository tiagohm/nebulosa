import type { Device } from './device.types'

export interface LightBox extends Device {
	enabled: boolean
	intensity: number
	minIntensity: number
	maxIntensity: number
}

export const DEFAULT_LIGHT_BOX: LightBox = {
	enabled: false,
	intensity: 0,
	minIntensity: 0,
	maxIntensity: 0,
	type: 'LIGHT_BOX',
	sender: '',
	id: '',
	name: '',
	driverName: '',
	driverVersion: '',
	connected: false,
}

export function isLightBox(device?: Device): device is LightBox {
	return !!device && device.type === 'LIGHT_BOX'
}
