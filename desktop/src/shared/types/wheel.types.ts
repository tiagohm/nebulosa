import type { CameraStartCapture } from './camera.types'
import type { Device } from './device.types'

export type WheelDialogMode = 'CAPTURE' | 'SEQUENCER' | 'FLAT_WIZARD'

export interface FilterWheel extends Device {
	count: number
	position: number
	moving: boolean
	names: string[]
}

export const EMPTY_WHEEL: FilterWheel = {
	sender: '',
	id: '',
	count: 0,
	position: 0,
	moving: false,
	name: '',
	connected: false,
	names: [],
}

export interface WheelDialogInput {
	mode: WheelDialogMode
	wheel: FilterWheel
	request: CameraStartCapture
}

export interface WheelPreference {
	shutterPosition?: number
}

export interface FilterSlot {
	position: number
	name: string
	dark: boolean
}

export interface WheelRenamed {
	wheel: FilterWheel
	filter: FilterSlot
}

export function makeFilterSlots(wheel: FilterWheel, filters: FilterSlot[], shutterPosition: number = 0) {
	if (wheel.count <= 0) {
		filters = []
	} else if (wheel.count !== filters.length) {
		filters = new Array<FilterSlot>(wheel.count)
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

export function isFilterWheel(device?: Device): device is FilterWheel {
	return !!device && 'count' in device
}
