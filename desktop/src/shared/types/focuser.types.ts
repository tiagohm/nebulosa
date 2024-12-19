import type { Thermometer } from './auxiliary.types'
import { EMPTY_DEVICE_SENDER, EMPTY_DRIVER_INFO, type Device } from './device.types'

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

export interface FocuserPreference {
	stepsRelative: number
	stepsAbsolute: number
}

export const DEFAULT_FOCUSER: Focuser = {
	type: 'FOCUSER',
	sender: EMPTY_DEVICE_SENDER,
	driver: EMPTY_DRIVER_INFO,
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

export const DEFAULT_FOCUSER_PREFERENCE: FocuserPreference = {
	stepsRelative: 100,
	stepsAbsolute: 0,
}

export function isFocuser(device?: Device): device is Focuser {
	return !!device && device.type === 'FOCUSER'
}

export function focuserPreferenceWithDefault(preference?: Partial<FocuserPreference>, source: FocuserPreference = DEFAULT_FOCUSER_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.stepsAbsolute ??= source.stepsAbsolute
	preference.stepsAbsolute ??= source.stepsAbsolute
	return preference as FocuserPreference
}
