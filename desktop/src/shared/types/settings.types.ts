export type PlateSolverType = 'ASTROMETRY_NET' | 'ASTROMETRY_NET_ONLINE' | 'ASTAP' | 'SIRIL'

export interface PlateSolverRequest {
	type: PlateSolverType
	executablePath: string
	downsampleFactor: number
	apiUrl: string
	apiKey: string
	timeout: number
}

export const EMPTY_PLATE_SOLVER_REQUEST: PlateSolverRequest = {
	type: 'ASTAP',
	executablePath: '',
	downsampleFactor: 0,
	apiUrl: 'https://nova.astrometry.net/',
	apiKey: '',
	timeout: 300,
}

export type StarDetectorType = 'ASTAP' | 'PIXINSIGHT' | 'SIRIL'

export interface StarDetectionRequest {
	type: StarDetectorType
	executablePath: string
	timeout: number
	minSNR: number
	maxStars: number
	slot: number
}

export const EMPTY_STAR_DETECTION_REQUEST: StarDetectionRequest = {
	type: 'ASTAP',
	executablePath: '',
	timeout: 300,
	minSNR: 0,
	maxStars: 0,
	slot: 1,
}
