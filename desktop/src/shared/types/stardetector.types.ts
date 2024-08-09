export type StarDetectorType = 'ASTAP' | 'PIXINSIGHT' | 'SIRIL'

export interface StarDetectorSettings {
	executablePath: string
	timeout: number
	slot: number
}

export interface StarDetectionRequest extends StarDetectorSettings {
	type: StarDetectorType
	minSNR?: number
	maxStars?: number
}

export const DEFAULT_STAR_DETECTOR_SETTINGS: StarDetectorSettings = {
	executablePath: '',
	timeout: 300,
	slot: 0,
}

export const DEFAULT_STAR_DETECTION_REQUEST: StarDetectionRequest = {
	...DEFAULT_STAR_DETECTOR_SETTINGS,
	type: 'ASTAP',
	minSNR: 0,
	maxStars: 0,
}

export function starDetectorSettingsWithDefault(settings?: Partial<StarDetectorSettings>, source: StarDetectorSettings = DEFAULT_STAR_DETECTOR_SETTINGS) {
	if (!settings) return structuredClone(source)
	settings.executablePath ||= source.executablePath
	settings.timeout ??= source.timeout
	settings.slot ??= source.slot
	return settings as StarDetectorSettings
}

export function starDetectionRequestWithDefault(request?: Partial<StarDetectionRequest>, source: StarDetectionRequest = DEFAULT_STAR_DETECTION_REQUEST) {
	if (!request) return structuredClone(source)
	starDetectorSettingsWithDefault(request, source)
	request.type ||= source.type
	request.minSNR ??= source.minSNR
	request.maxStars ??= source.maxStars
	return request as StarDetectionRequest
}
