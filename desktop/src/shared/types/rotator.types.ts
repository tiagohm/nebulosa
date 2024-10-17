import type { CameraStartCapture } from './camera.types'
import type { Device } from './device.types'

export type RotatorDialogMode = 'CAPTURE' | 'SEQUENCER'

export interface RotatorDialogInput {
	mode: RotatorDialogMode
	rotator: Rotator
	request: CameraStartCapture
}

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

export interface RotatorPreference {
	angle: number
}

export const DEFAULT_ROTATOR: Rotator = {
	type: 'ROTATOR',
	sender: '',
	driverName: '',
	driverVersion: '',
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

export const DEFAULT_ROTATOR_PREFERENCE: RotatorPreference = {
	angle: 0,
}

export function isRotator(device?: Device): device is Rotator {
	return !!device && device.type === 'ROTATOR'
}

export function rotatorPreferenceWithDefault(preference?: Partial<RotatorPreference>, source: RotatorPreference = DEFAULT_ROTATOR_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.angle ??= source.angle
	return preference as RotatorPreference
}
