import type { Angle } from './atlas.types'
import { cameraStartCaptureWithDefault, DEFAULT_CAMERA_START_CAPTURE, type Camera, type CameraCaptureEvent, type CameraStartCapture } from './camera.types'
import type { GuideDirection } from './guider.types'
import { DEFAULT_PLATE_SOLVER_REQUEST, plateSolverRequestWithDefault, type PlateSolverRequest } from './platesolver.types'

export type Hemisphere = 'NORTHERN' | 'SOUTHERN'

export type DARVState = 'IDLE' | 'INITIAL_PAUSE' | 'FORWARD' | 'BACKWARD'

export type TPPAState = 'IDLE' | 'SLEWING' | 'SLEWED' | 'SETTLING' | 'EXPOSURING' | 'SOLVING' | 'SOLVED' | 'COMPUTED' | 'PAUSING' | 'PAUSED' | 'FINISHED' | 'FAILED'

export type AlignmentMethod = 'DARV' | 'TPPA'

export interface AlignmentPreference {
	darvHemisphere: Hemisphere
	darvRequest: DARVStart
	tppaRequest: TPPAStart
}

export interface DARVStart {
	capture: CameraStartCapture
	direction: GuideDirection
	reversed: boolean
}

export interface DARVEvent extends MessageEvent {
	camera: Camera
	state: DARVState
	direction?: GuideDirection
	capture: CameraCaptureEvent
}

export interface TPPAStart {
	capture: CameraStartCapture
	plateSolver: PlateSolverRequest
	startFromCurrentPosition: boolean
	compensateRefraction: boolean
	stopTrackingWhenDone: boolean
	stepDirection: GuideDirection
	stepDuration: number
	stepSpeed?: string
}

export interface TPPAResult {
	failed: boolean
	rightAscension: Angle
	declination: Angle
	azimuthError: Angle
	azimuthErrorDirection: string
	altitudeError: Angle
	altitudeErrorDirection: string
	totalError: Angle
}

export interface TPPAEvent extends MessageEvent {
	camera: Camera
	state: TPPAState
	rightAscension: Angle
	declination: Angle
	azimuthError: Angle
	altitudeError: Angle
	totalError: Angle
	azimuthErrorDirection: string
	altitudeErrorDirection: string
	capture?: CameraCaptureEvent
}

export interface DARVResult {
	direction?: GuideDirection
}

export const DEFAULT_CAMERA_START_CAPTURE_TPPA: CameraStartCapture = {
	...DEFAULT_CAMERA_START_CAPTURE,
}

export const DEFAULT_TPPA_START: TPPAStart = {
	capture: DEFAULT_CAMERA_START_CAPTURE_TPPA,
	plateSolver: DEFAULT_PLATE_SOLVER_REQUEST,
	startFromCurrentPosition: true,
	stepDirection: 'EAST',
	compensateRefraction: true,
	stopTrackingWhenDone: true,
	stepDuration: 5,
}

export const DEFAULT_TPPA_RESULT: TPPAResult = {
	failed: false,
	rightAscension: `00h00m00s`,
	declination: `00°00'00"`,
	azimuthError: `00°00'00"`,
	azimuthErrorDirection: '',
	altitudeError: `00°00'00"`,
	altitudeErrorDirection: '',
	totalError: `00°00'00"`,
}

export const DEFAULT_CAMERA_START_CAPTURE_DARV: CameraStartCapture = {
	...DEFAULT_CAMERA_START_CAPTURE,
	exposureDelay: 5,
	exposureTime: 30000000,
}

export const DEFAULT_DARV_START: DARVStart = {
	capture: DEFAULT_CAMERA_START_CAPTURE_DARV,
	direction: 'NORTH',
	reversed: false,
}

export const DEFAULT_DARV_RESULT: DARVResult = {}

export const DEFAULT_ALIGNMENT_PREFERENCE: AlignmentPreference = {
	darvHemisphere: 'NORTHERN',
	darvRequest: DEFAULT_DARV_START,
	tppaRequest: DEFAULT_TPPA_START,
}

export function darvStartWithDefault(request?: Partial<DARVStart>, source: DARVStart = DEFAULT_DARV_START) {
	if (!request) return structuredClone(source)
	request.capture = cameraStartCaptureWithDefault(request.capture, source.capture)
	request.direction ||= source.direction
	request.reversed ??= source.reversed
	return request as DARVStart
}

export function tppaStartWithDefault(request?: Partial<TPPAStart>, source: TPPAStart = DEFAULT_TPPA_START) {
	if (!request) return structuredClone(source)
	request.capture = cameraStartCaptureWithDefault(request.capture, source.capture)
	request.plateSolver = plateSolverRequestWithDefault(request.plateSolver, source.plateSolver)
	request.startFromCurrentPosition ??= source.startFromCurrentPosition
	request.stepDirection ||= source.stepDirection
	request.compensateRefraction ??= source.compensateRefraction
	request.stopTrackingWhenDone ??= source.stopTrackingWhenDone
	request.stepDuration ??= source.stepDuration
	return request as TPPAStart
}

export function alignmentPreferenceWithDefault(preference?: Partial<AlignmentPreference>, source: AlignmentPreference = DEFAULT_ALIGNMENT_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.darvHemisphere ||= source.darvHemisphere
	preference.darvRequest = darvStartWithDefault(preference.darvRequest, source.darvRequest)
	preference.tppaRequest = tppaStartWithDefault(preference.tppaRequest, source.tppaRequest)
	return preference as AlignmentPreference
}
