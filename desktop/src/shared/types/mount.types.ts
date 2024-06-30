import type { Angle, EquatorialCoordinate } from './atlas.types'
import type { Device } from './device.types'
import type { GPS } from './gps.types'
import type { GuideOutput } from './guider.types'

export type PierSide = 'EAST' | 'WEST' | 'NEITHER'

export type TargetCoordinateType = 'J2000' | 'JNOW'

export type TrackMode = 'SIDEREAL' | ' LUNAR' | 'SOLAR' | 'KING' | 'CUSTOM'

export type CelestialLocationType = 'ZENITH' | 'NORTH_POLE' | 'SOUTH_POLE' | 'GALACTIC_CENTER' | 'MERIDIAN_EQUATOR' | 'MERIDIAN_ECLIPTIC' | 'EQUATOR_ECLIPTIC'

export type MountRemoteControlType = 'LX200' | 'STELLARIUM'

export type MoveDirectionType = 'N' | 'S' | 'W' | 'E' | 'NW' | 'NE' | 'SW' | 'SE'

export interface SlewRate {
	name: string
	label: string
}

export interface Parkable {
	canPark: boolean
	parking: boolean
	parked: boolean
}

export interface Mount extends EquatorialCoordinate, GPS, GuideOutput, Parkable {
	slewing: boolean
	tracking: boolean
	canAbort: boolean
	canSync: boolean
	canGoTo: boolean
	canHome: boolean
	slewRates: SlewRate[]
	slewRate?: SlewRate
	trackModes: TrackMode[]
	trackMode: TrackMode
	pierSide: PierSide
	guideRateWE: number
	guideRateNS: number
}

export const EMPTY_MOUNT: Mount = {
	sender: '',
	id: '',
	slewing: false,
	tracking: false,
	canAbort: false,
	canSync: false,
	canGoTo: false,
	canHome: false,
	slewRates: [],
	trackModes: [],
	trackMode: 'SIDEREAL',
	pierSide: 'NEITHER',
	guideRateWE: 0,
	guideRateNS: 0,
	rightAscension: '00h00m00s',
	declination: `00°00'00"`,
	hasGPS: false,
	longitude: 0,
	latitude: 0,
	elevation: 0,
	dateTime: 0,
	offsetInMinutes: 0,
	name: '',
	connected: false,
	canPulseGuide: false,
	pulseGuiding: false,
	canPark: false,
	parking: false,
	parked: false,
}

export interface MountRemoteControl {
	type: MountRemoteControlType
	mount: Mount
	running: boolean
	rightAscension: Angle
	declination: Angle
	latitude: Angle
	longitude: Angle
	slewing: boolean
	tracking: boolean
	parked: boolean
	host: string
	port: number
}

export interface MountRemoteControlDialog {
	showDialog: boolean
	type: MountRemoteControlType
	host: string
	port: number
	data: MountRemoteControl[]
}

export interface MountPreference {
	targetCoordinateType: TargetCoordinateType
	targetRightAscension: Angle
	targetDeclination: Angle
}

export const EMPTY_MOUNT_PREFERENCE: MountPreference = {
	targetCoordinateType: 'JNOW',
	targetRightAscension: '',
	targetDeclination: '',
}

export function isMount(device?: Device): device is Mount {
	return !!device && 'tracking' in device
}
