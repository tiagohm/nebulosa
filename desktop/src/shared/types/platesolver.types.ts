export type PlateSolverType = 'ASTROMETRY_NET' | 'ASTROMETRY_NET_ONLINE' | 'ASTAP' | 'SIRIL' | 'PIXINSIGHT'

export interface PlateSolverRequest {
	type: PlateSolverType
	executablePath: string
	downsampleFactor: number
	apiUrl: string
	apiKey: string
	timeout: number
	slot: number
	pixelSize?: number
	focalLength?: number
}

export const NOVA_ASTROMETRY_NET_URL = 'https://nova.astrometry.net/'

export const EMPTY_PLATE_SOLVER_REQUEST: PlateSolverRequest = {
	type: 'ASTAP',
	executablePath: '',
	downsampleFactor: 0,
	apiUrl: NOVA_ASTROMETRY_NET_URL,
	apiKey: '',
	timeout: 300,
	slot: 1,
}
