import type { Device } from './device.types'

export interface Rotator extends Device {
	moving: boolean
	angle: number
	canAbort: boolean
	canReverse: boolean
	reversed: boolean
	canSync: boolean
	canHome: boolean
	hasBacklashCompensation: boolean
	minAngle: number
	maxAngle: number
}

export const EMPTY_ROTATOR: Rotator = {
	sender: '',
	id: '',
	name: '',
	moving: false,
	angle: 0,
	canAbort: false,
	canReverse: false,
	reversed: false,
	canSync: false,
	canHome: false,
	hasBacklashCompensation: false,
	minAngle: 0,
	maxAngle: 0,
	connected: false,
}

export interface RotatorPreference {
	angle?: number
}

export function isRotator(device?: Device): device is Rotator {
	return !!device && 'angle' in device
}
