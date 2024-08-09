import type { DeviceType } from './device.types'

export type HomeWindowType = DeviceType | 'GUIDER' | 'SKY_ATLAS' | 'ALIGNMENT' | 'SEQUENCER' | 'IMAGE' | 'FRAMING' | 'INDI' | 'SETTINGS' | 'CALCULATOR' | 'ABOUT' | 'FLAT_WIZARD' | 'AUTO_FOCUS' | 'STACKER' | 'CALIBRATION'

export type ConnectionType = 'INDI' | 'ALPACA'

export type ConnectionStatus = Omit<Required<ConnectionDetails>, 'connected' | 'name' | 'connectedAt'>

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

export interface ConnectionClosed {
	id: string
}

export interface HomePreference {
	connections: ConnectionDetails[]
	imagePath?: string
}

export interface HomeConnectionDialog {
	showDialog: boolean
	connection: ConnectionDetails
	edited: boolean
}

export const DEFAULT_CONNECTION_HOST: string = 'localhost'
export const DEFAULT_CONNECTION_PORT: number = 7624

export const DEFAULT_CONNECTION_DETAILS: ConnectionDetails = {
	name: 'Local',
	host: DEFAULT_CONNECTION_HOST,
	port: DEFAULT_CONNECTION_PORT,
	type: 'INDI',
	connected: false,
}

export const DEFAULT_HOME_PREFERENCE: HomePreference = {
	connections: [],
}

export const DEFAULT_HOME_CONNECTION_DIALOG: HomeConnectionDialog = {
	showDialog: false,
	edited: false,
	connection: DEFAULT_CONNECTION_DETAILS,
}

export function homePreferenceWithDefault(preference?: Partial<HomePreference>, source: HomePreference = DEFAULT_HOME_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.connections ??= source.connections
	preference.imagePath ??= source.imagePath
	return preference as HomePreference
}
