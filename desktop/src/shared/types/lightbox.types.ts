import type { Device } from './device.types'

export interface LightBox extends Device {
	enabled: boolean
	intensity: number
	minIntensity: number
	maxIntensity: number
}

export interface LightBoxPreference {
	intensity: number
}

export const DEFAULT_LIGHT_BOX_PREFERENCE: LightBoxPreference = {
	intensity: 0,
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

export function lightBoxPreferenceWithDefault(preference?: Partial<LightBoxPreference>, source: LightBoxPreference = DEFAULT_LIGHT_BOX_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.intensity ??= source.intensity
	return preference as LightBoxPreference
}
