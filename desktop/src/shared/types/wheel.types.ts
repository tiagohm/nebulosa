import type { CameraStartCapture } from './camera.types'
import type { Device } from './device.types'

export type WheelDialogMode = 'CAPTURE' | 'SEQUENCER' | 'FLAT_WIZARD'

export interface Wheel extends Device {
	count: number
	position: number
	moving: boolean
	names: string[]
}

export interface WheelDialogInput {
	mode: WheelDialogMode
	wheel: Wheel
	request: CameraStartCapture
}

export interface WheelPreference {
	shutterPosition: number
}

export interface Filter {
	position: number
	name: string
	dark: boolean
}

export interface WheelRenamed {
	wheel: Wheel
	filter: Filter
}

export function makeFilter(wheel: Wheel, filters: Filter[], shutterPosition: number = 0) {
	if (wheel.count <= 0) {
		filters = []
	} else if (wheel.count !== filters.length) {
		filters = new Array<Filter>(wheel.count)
	}

	if (filters.length) {
		for (let position = 0; position < filters.length; position++) {
			const name = wheel.names[position] || `Filter #${position}`
			const dark = position + 1 === shutterPosition

			if (!filters[position]) {
				filters[position] = { position: position + 1, name, dark }
			} else {
				filters[position].dark = dark
				filters[position].name = name
			}
		}
	}

	return filters
}

export const DEFAULT_WHEEL: Wheel = {
	type: 'WHEEL',
	sender: '',
	driverName: '',
	driverVersion: '',
	id: '',
	count: 0,
	position: 0,
	moving: false,
	name: '',
	connected: false,
	names: [],
}

export const DEFAULT_WHEEL_PREFERENCE: WheelPreference = {
	shutterPosition: 0,
}

export function isWheel(device?: Device): device is Wheel {
	return !!device && device.type === 'WHEEL'
}

export function wheelPreferenceWithDefault(preference?: Partial<WheelPreference>, source: WheelPreference = DEFAULT_WHEEL_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.shutterPosition ??= source.shutterPosition
	return preference as WheelPreference
}
