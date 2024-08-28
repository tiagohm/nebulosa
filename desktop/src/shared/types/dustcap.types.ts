import type { Device } from './device.types'
import type { Parkable } from './mount.types'

export type DustCap = Device & Parkable

export const DEFAULT_DUST_CAP: DustCap = {
	type: 'DUST_CAP',
	sender: '',
	id: '',
	name: '',
	driverName: '',
	driverVersion: '',
	connected: false,
	canPark: false,
	parking: false,
	parked: false,
}

export function isDustCap(device?: Device): device is DustCap {
	return !!device && device.type === 'DUST_CAP'
}
