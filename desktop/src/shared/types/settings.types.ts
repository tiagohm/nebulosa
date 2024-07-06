import type { FrameType } from './camera.types'

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

export interface CameraCaptureNamingFormat {
	light?: string
	dark?: string
	flat?: string
	bias?: string
}

export const DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT: CameraCaptureNamingFormat = {
	light: '[camera]_[type]_[year:2][month][day][hour][min][sec][ms]_[filter]_[width]_[height]_[exp]_[bin]_[gain]',
	dark: '[camera]_[type]_[width]_[height]_[exp]_[bin]_[gain]',
	flat: '[camera]_[type]_[filter]_[width]_[height]_[bin]',
	bias: '[camera]_[type]_[width]_[height]_[bin]_[gain]',
}

export function resetCameraCaptureNamingFormat(type: FrameType, format: CameraCaptureNamingFormat, defaultValue?: CameraCaptureNamingFormat) {
	switch (type) {
		case 'LIGHT':
			format.light = defaultValue?.light ?? DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.light
			break
		case 'DARK':
			format.dark = defaultValue?.dark ?? DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.dark
			break
		case 'FLAT':
			format.flat = defaultValue?.flat ?? DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.flat
			break
		case 'BIAS':
			format.bias = defaultValue?.bias ?? DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.bias
			break
	}
}
