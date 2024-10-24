import type { Angle } from './atlas.types'

export interface HipsSurvey {
	id: string
	category: string
	frame: string
	regime: string
	bitPix: number
	pixelScale: number
	skyFraction: number
}

export interface FramingPreference {
	rightAscension: Angle
	declination: Angle
	width: number
	height: number
	fov: number
	rotation: number
	hipsSurvey?: HipsSurvey
}

export interface LoadFraming {
	rightAscension: Angle
	declination: Angle
	width?: number
	height?: number
	fov?: number
	rotation?: number
}

export const DEFAULT_FRAMING_PREFERENCE: FramingPreference = {
	rightAscension: '00h00m00s',
	declination: `000°00'00"`,
	width: 1024,
	height: 720,
	fov: 1,
	rotation: 0,
}

export function framingPreferenceWithDefault(preference?: Partial<FramingPreference>, source: FramingPreference = DEFAULT_FRAMING_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.rightAscension ??= source.rightAscension
	preference.declination ??= source.declination
	preference.width ??= source.width
	preference.height ??= source.height
	preference.fov ??= source.fov
	preference.rotation ??= source.rotation
	preference.hipsSurvey ??= source.hipsSurvey
	return preference as FramingPreference
}
