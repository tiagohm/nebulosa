import { EquatorialCoordinate } from './atlas.types'
import { GPS } from './gps.types'
import { GuideOutput } from './guider.types'

export type PierSide = 'EAST' | 'WEST' | 'NEITHER'

export type TargetCoordinateType = 'J2000' | 'JNOW'

export type TrackMode = 'SIDEREAL' | ' LUNAR' | 'SOLAR' | 'KING' | 'CUSTOM'

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
    parked: false
}
