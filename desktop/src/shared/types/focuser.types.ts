import type { Thermometer } from './auxiliary.types'
import type { Device } from './device.types'

export interface Focuser extends Device, Thermometer {
	moving: boolean
	position: number
	canAbsoluteMove: boolean
	canRelativeMove: boolean
	canAbort: boolean
	canReverse: boolean
	reversed: boolean
	canSync: boolean
	hasBacklash: boolean
	maxPosition: number
}

export const EMPTY_FOCUSER: Focuser = {
	sender: '',
	id: '',
	moving: false,
	position: 0,
	canAbsoluteMove: false,
	canRelativeMove: false,
	canAbort: false,
	canReverse: false,
	reversed: false,
	canSync: false,
	hasBacklash: false,
	maxPosition: 0,
	name: '',
	connected: false,
	hasThermometer: false,
	temperature: 0,
}

export interface FocuserPreference {
	stepsRelative?: number
	stepsAbsolute?: number
}

export function isFocuser(device?: Device): device is Focuser {
	return !!device && 'maxPosition' in device
}
