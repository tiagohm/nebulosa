export type StarDetectorType = 'ASTAP' | 'PIXINSIGHT' | 'SIRIL'

export interface StarDetectionRequest {
	type: StarDetectorType
	executablePath: string
	timeout: number
	minSNR?: number
	maxStars?: number
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
