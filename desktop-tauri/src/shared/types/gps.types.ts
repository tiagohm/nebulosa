import type { Device } from './device.types'

export interface GPS extends Device {
	hasGPS: boolean
	longitude: number
	latitude: number
	elevation: number
	dateTime: number
	offsetInMinutes: number
}
