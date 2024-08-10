import type { Point } from 'electron'
import { cameraStartCaptureWithDefault, DEFAULT_CAMERA_START_CAPTURE, type CameraCaptureEvent, type CameraStartCapture } from './camera.types'
import type { StarDetectionRequest } from './stardetector.types'
import { DEFAULT_STAR_DETECTION_REQUEST, starDetectionRequestWithDefault } from './stardetector.types'

export type AutoFocusState = 'IDLE' | 'MOVING' | 'EXPOSURING' | 'EXPOSURED' | 'ANALYSING' | 'ANALYSED' | 'CURVE_FITTED' | 'FAILED' | 'FINISHED'

export type AutoFocusFittingMode = 'TRENDLINES' | 'PARABOLIC' | 'TREND_PARABOLIC' | 'HYPERBOLIC' | 'TREND_HYPERBOLIC'

export type BacklashCompensationMode = 'NONE' | 'ABSOLUTE' | 'OVERSHOOT'

export interface BacklashCompensation {
	mode: BacklashCompensationMode
	backlashIn: number
	backlashOut: number
}

export interface AutoFocusRequest {
	fittingMode: AutoFocusFittingMode
	capture: CameraStartCapture
	rSquaredThreshold: number
	backlashCompensation: BacklashCompensation
	initialOffsetSteps: number
	stepSize: number
	totalNumberOfAttempts: number
	starDetector: StarDetectionRequest
}

export interface AutoFocusPreference {
	request: AutoFocusRequest
}

export interface Curve {
	minimum: Point
	rSquared: number
}

export interface Plottable {
	points: Point[]
}

export interface HyperbolicCurve extends Curve, Plottable {
	a: number
	b: number
	p: number
}

export interface ParabolicCurve extends Curve, Plottable {}

export interface Line extends Plottable {
	slope: number
	intercept: number
	rSquared: number
}

export interface TrendLineCurve extends Curve {
	left: Line
	right: Line
	intersection: Point
}

export interface AutoFocusChart {
	predictedFocusPoint?: Point
	minX: number
	maxX: number
	minY: number
	maxY: number
	trendLine?: TrendLineCurve
	parabolic?: ParabolicCurve
	hyperbolic?: HyperbolicCurve
}

export interface AutoFocusEvent {
	state: AutoFocusState
	focusPoint?: Point
	determinedFocusPoint?: Point
	starCount: number
	starHFD: number
	chart?: AutoFocusChart
	capture?: CameraCaptureEvent
}

export const DEFAULT_CAMERA_START_CAPTURE_AUTO_FOCUS: CameraStartCapture = {
	...DEFAULT_CAMERA_START_CAPTURE,
}

export const DEFAULT_BACKLASH_COMPENSATION: BacklashCompensation = {
	mode: 'NONE',
	backlashIn: 0,
	backlashOut: 0,
}

export const DEFAULT_AUTO_FOCUS_REQUEST: AutoFocusRequest = {
	capture: DEFAULT_CAMERA_START_CAPTURE_AUTO_FOCUS,
	fittingMode: 'HYPERBOLIC',
	rSquaredThreshold: 0.5,
	initialOffsetSteps: 4,
	stepSize: 100,
	totalNumberOfAttempts: 1,
	backlashCompensation: DEFAULT_BACKLASH_COMPENSATION,
	starDetector: DEFAULT_STAR_DETECTION_REQUEST,
}

export const DEFAULT_AUTO_FOCUS_PREFERENCE: AutoFocusPreference = {
	request: DEFAULT_AUTO_FOCUS_REQUEST,
}

export function backlashCompensationWithDefault(compensation?: Partial<BacklashCompensation>, source: BacklashCompensation = DEFAULT_BACKLASH_COMPENSATION) {
	if (!compensation) return structuredClone(source)
	compensation.mode ||= source.mode
	compensation.backlashIn ??= source.backlashIn
	compensation.backlashOut ??= source.backlashOut
	return compensation as BacklashCompensation
}

export function autoFocusRequestWithDefault(request?: Partial<AutoFocusRequest>, source: AutoFocusRequest = DEFAULT_AUTO_FOCUS_REQUEST) {
	if (!request) return structuredClone(source)
	request.capture = cameraStartCaptureWithDefault(request.capture, source.capture)
	request.fittingMode ??= source.fittingMode
	request.rSquaredThreshold ??= source.rSquaredThreshold
	request.initialOffsetSteps ??= source.initialOffsetSteps
	request.stepSize ??= source.stepSize
	request.totalNumberOfAttempts ??= source.totalNumberOfAttempts
	request.backlashCompensation = backlashCompensationWithDefault(request.backlashCompensation, source.backlashCompensation)
	request.starDetector = starDetectionRequestWithDefault(request.starDetector, source.starDetector)
	return request as AutoFocusRequest
}

export function autoFocusPreferenceWithDefault(preference?: Partial<AutoFocusPreference>, source: AutoFocusPreference = DEFAULT_AUTO_FOCUS_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.request = autoFocusRequestWithDefault(preference.request, source.request)
	return preference as AutoFocusPreference
}
