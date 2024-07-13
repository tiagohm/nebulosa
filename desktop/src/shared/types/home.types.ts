import type { Camera } from './camera.types'
import type { DeviceType } from './device.types'
import type { Focuser } from './focuser.types'
import type { Mount } from './mount.types'
import type { Rotator } from './rotator.types'
import type { FilterWheel } from './wheel.types'

export type HomeWindowType = DeviceType | 'GUIDER' | 'SKY_ATLAS' | 'ALIGNMENT' | 'SEQUENCER' | 'IMAGE' | 'FRAMING' | 'INDI' | 'SETTINGS' | 'CALCULATOR' | 'ABOUT' | 'FLAT_WIZARD' | 'AUTO_FOCUS' | 'STACKER'

export const CONNECTION_TYPES = ['INDI', 'ALPACA'] as const

export type ConnectionType = (typeof CONNECTION_TYPES)[number]

export interface ConnectionDetails {
	name: string
	host: string
	port: number
	ip?: string
	type: ConnectionType
	connected: boolean
	connectedAt?: number
	id?: string
}

export type ConnectionStatus = Omit<Required<ConnectionDetails>, 'connected' | 'name' | 'connectedAt'>

export const EMPTY_CONNECTION_DETAILS: ConnectionDetails = {
	name: '',
	host: 'localhost',
	port: 7624,
	type: 'INDI',
	connected: false,
}

export interface ConnectionClosed {
	id: string
}

export interface HomePreference {
	imagePath?: string
}

export interface Equipment {
	camera?: Camera
	guider?: Camera
	mount?: Mount
	focuser?: Focuser
	wheel?: FilterWheel
	rotator?: Rotator
}
