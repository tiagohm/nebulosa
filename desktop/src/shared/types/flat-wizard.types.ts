import { cameraStartCaptureWithDefault, DEFAULT_CAMERA_START_CAPTURE, type CameraCaptureEvent, type CameraStartCapture } from './camera.types'

export type FlatWizardState = 'EXPOSURING' | 'CAPTURED' | 'FAILED'

export interface FlatWizardPreference {
	request: FlatWizardRequest
}

export interface FlatWizardRequest {
	capture: CameraStartCapture
	exposureMin: number
	exposureMax: number
	meanTarget: number
	meanTolerance: number
}

export interface FlatWizardEvent {
	state: FlatWizardState
	exposureTime: number
	capture?: CameraCaptureEvent
	savedPath?: string
}

export const DEFAULT_CAMERA_START_CAPTURE_FLAT_WIZARD: CameraStartCapture = {
	...DEFAULT_CAMERA_START_CAPTURE,
	frameType: 'FLAT',
}

export const DEFAULT_FLAT_WIZARD_REQUEST: FlatWizardRequest = {
	capture: DEFAULT_CAMERA_START_CAPTURE_FLAT_WIZARD,
	exposureMin: 1,
	exposureMax: 2000,
	meanTarget: 32768,
	meanTolerance: 10,
}

export const DEFAULT_FLAT_WIZARD_PREFERENCE: FlatWizardPreference = {
	request: DEFAULT_FLAT_WIZARD_REQUEST,
}

export function flatWizardRequestWithDefault(request?: Partial<FlatWizardRequest>, source: FlatWizardRequest = DEFAULT_FLAT_WIZARD_REQUEST) {
	if (!request) return structuredClone(source)
	request.capture = cameraStartCaptureWithDefault(request.capture, source.capture)
	request.exposureMin ??= source.exposureMin
	request.exposureMax ??= source.exposureMax
	request.meanTarget ??= source.meanTarget
	request.meanTolerance ??= source.meanTolerance
	return request as FlatWizardRequest
}

export function flatWizardPreferenceWithDefault(preference?: Partial<FlatWizardPreference>, source: FlatWizardPreference = DEFAULT_FLAT_WIZARD_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.request = flatWizardRequestWithDefault(preference.request, source.request)
	return preference as FlatWizardPreference
}
