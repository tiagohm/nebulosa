import type { Angle } from './atlas.types'

export type PlateSolverType = 'ASTROMETRY_NET' | 'ASTROMETRY_NET_ONLINE' | 'ASTAP' | 'SIRIL' | 'PIXINSIGHT'

export interface PlateSolverSettings {
	executablePath: string
	downsampleFactor: number
	apiUrl: string
	apiKey: string
	timeout: number
	slot: number
}

export interface PlateSolverRequest extends PlateSolverSettings {
	type: PlateSolverType
	blind: boolean
	centerRA: Angle
	centerDEC: Angle
	radius: number
	pixelSize: number
	focalLength: number
	width: number
	height: number
}

export const NOVA_ASTROMETRY_NET_URL = 'https://nova.astrometry.net/'

export const DEFAULT_PLATE_SOLVER_SETTINGS: PlateSolverSettings = {
	executablePath: '',
	downsampleFactor: 0,
	apiUrl: NOVA_ASTROMETRY_NET_URL,
	apiKey: '',
	timeout: 300,
	slot: 1,
}

export const DEFAULT_PLATE_SOLVER_REQUEST: PlateSolverRequest = {
	...DEFAULT_PLATE_SOLVER_SETTINGS,
	type: 'ASTAP',
	blind: true,
	centerRA: 0,
	centerDEC: 0,
	radius: 4,
	focalLength: 0,
	pixelSize: 0,
	width: 0,
	height: 0,
}

export function plateSolverSettingsWithDefault(settings?: Partial<PlateSolverSettings>, source: PlateSolverSettings = DEFAULT_PLATE_SOLVER_SETTINGS) {
	if (!settings) return structuredClone(source)
	settings.executablePath ||= source.executablePath
	settings.downsampleFactor ??= source.downsampleFactor
	settings.apiUrl ||= source.apiUrl
	settings.apiKey ||= source.apiKey
	settings.timeout ??= source.timeout
	settings.slot ??= source.slot
	return settings as PlateSolverSettings
}

export function plateSolverRequestWithDefault(request?: Partial<PlateSolverRequest>, source: PlateSolverRequest = DEFAULT_PLATE_SOLVER_REQUEST) {
	if (!request) return structuredClone(source)
	plateSolverSettingsWithDefault(request, source)
	request.type ??= source.type
	request.pixelSize ??= source.pixelSize
	request.focalLength ??= source.focalLength
	return request as PlateSolverRequest
}
