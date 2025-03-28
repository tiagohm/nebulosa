import type { GeographicCoordinate } from './atlas.types'
import type { Device } from './device.types'

export interface GPS extends Device, GeographicCoordinate<number> {
	hasGPS: boolean
	dateTime: number
	offsetInMinutes: number
}
